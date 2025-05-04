package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.models.FileResult;
import co.edu.upb.app.domain.models.OfficeFile;
import co.edu.upb.app.domain.models.storage.Conversion;
import co.edu.upb.app.domain.models.storage.TransactionIteration;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.node.domain.models.ConvertedFile;
import co.edu.upb.node.domain.models.File;
import co.edu.upb.node.domain.models.Iteration;
import co.edu.upb.node.domain.models.NodeReport;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ConversionManager extends UnicastRemoteObject implements IConversionManager, InterfacePublisher {

    private final IMetricsManager metricsManager;
    private final ArrayList<InterfaceNode> nodes;

    private double weightCpu;        // a
    private double weightActive;     // b
    private double weightQueue;      // c
    private double weightFileSize;   // d

    public ConversionManager(IMetricsManager metricsManager) throws RemoteException {
        this(metricsManager, 0.25, 0.25, 0.25, 0.25);
    }

    public ConversionManager(IMetricsManager metricsManager,
                             double weightCpu,
                             double weightActive,
                             double weightQueue,
                             double weightFileSize) throws RemoteException {
        super();
        this.metricsManager = metricsManager;
        this.nodes = new ArrayList<>();
        setWeights(weightCpu, weightActive, weightQueue, weightFileSize);
    }

    public void setWeights(double a, double b, double c, double d) {
        double sum = a + b + c + d;
        if (sum <= 0) throw new IllegalArgumentException("Sum of weights must be positive");
        this.weightCpu      = a / sum;
        this.weightActive   = b / sum;
        this.weightQueue    = c / sum;
        this.weightFileSize = d / sum;
    }

    @Override
    public AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException {
        this.nodes.add(node);
        return new AppResponse<Boolean>(true, "Node succesfully subscribed", true);
    }

    @Override
    public AppResponse<ConvertedFile[]> queueOfficeConversion(OfficeFile[] files, Integer userId) {
        Instant start = Instant.now();
        AppResponse<ConvertedFile[]> result;
        try {
            AppResponse<List<AppResponse<File>>> response = queueAlgorithm(files, 1, userId);
            ConvertedFile[] arr = getConvertedFiles(response);
            result = new AppResponse<>(true, response.getMessage(), arr);
        } catch (Exception e) {
            e.printStackTrace();
            result = new AppResponse<>(false, "Office files couldn't be converted", new ConvertedFile[0]);
        }
        long durationMs = Duration.between(start, Instant.now()).toMillis();
        logWeightsAndLatency(durationMs);
        return result;
    }

    @Override
    public AppResponse<ConvertedFile[]> queueURLConversion(String[] files, Integer userId) {
        Instant start = Instant.now();
        AppResponse<ConvertedFile[]> result;
        try {
            AppResponse<List<AppResponse<File>>> response = queueAlgorithm(files, 2, userId);
            ConvertedFile[] arr = getConvertedFiles(response);
            result = new AppResponse<>(true, response.getMessage(), arr);
        } catch (Exception e) {
            e.printStackTrace();
            result = new AppResponse<>(false, "URLs couldn't be converted", new ConvertedFile[0]);
        }
        long durationMs = Duration.between(start, Instant.now()).toMillis();
        logWeightsAndLatency(durationMs);
        return result;
    }


    private static ConvertedFile[] getConvertedFiles(AppResponse<List<AppResponse<File>>> response) {
        assert response.isSuccess(); //Response has to be successful

        List<AppResponse<File>> filesList = response.getData();

        //SECOND: We map the date into an array of ConvertedFile instances.
        List<ConvertedFile> convertedFiles = new ArrayList<>();

        for (AppResponse<File> file : filesList){
            //For each file, we're adding a list of converted files
            convertedFiles.add(new ConvertedFile(file.getData().data(), file.getData().originalFileName(), file.isSuccess()));
        }

        //THIRD: We convert the arraylist into an array and wrap it into an AppResponse instance.
        return convertedFiles.toArray(new ConvertedFile[0]);
    }

    private void storeMetadata(Map<AppResponse<File>, List<AppResponse<Iteration>>> transactions, Integer userId, String timestampQuery) {
        //We map the data into the required format
        //FIRST: We setup an array of TransactionIteration instances.
        List<Conversion> conversions = new ArrayList<>();

        //Map AppResponse<Iteration> instances into TransactionIteration instances
        for (Map.Entry<AppResponse<File>, List<AppResponse<Iteration>>> transaction: transactions.entrySet()) {
            List<AppResponse<Iteration>> iterationList = transaction.getValue();
            List<TransactionIteration> transactionIterationList = new ArrayList<>();

            //Map Iteration instances to TransactionIteration instances.
            for (AppResponse<Iteration> iteration : iterationList) {
                transactionIterationList.add(new TransactionIteration(
                        iteration.getData().startTimestamp(), iteration.isSuccess(), iteration.getMessage(), iteration.getData().endTimestamp(), iteration.getData().nodeId()
                ));
            }

            //Add the conversions to the list.
            AppResponse<File> fileAppResponse = transaction.getKey();
            conversions.add(new Conversion(userId,fileAppResponse.getData().size(), fileAppResponse.getData().fileTypeId(), fileAppResponse.getData().timestamp(), fileAppResponse.isSuccess(), transactionIterationList.toArray(new TransactionIteration[0])));
        }

        //Store the metadata on the storage server.
        AppResponse<Boolean> response = this.metricsManager.storeMetadata(new Transaction(
                timestampQuery, conversions.toArray(new Conversion[0])
        ));

        //Log the transaction
        System.out.println("Storage server with success on "+response.isSuccess() + " with message " + response.getMessage() + " at " + Instant.now().toString());
    }

    private <T> AppResponse<List<AppResponse<File>>> queueAlgorithm(T[] files, int strategy, int userId) {
        List<AppResponse<File>> fileResponses = new ArrayList<>(); //Files to be returned
        Map<AppResponse<File>, List<AppResponse<Iteration>>> transactions = new HashMap<>(); //Transaction for storeMetadata
        Instant time = Instant.now();

        //When empty, then no resources are available
        if (nodes.isEmpty()) {
            storeMetadata(transactions, userId, time.toString());
            return new AppResponse<>(true, "No resources available for converting", fileResponses);
        }

        //Fetch all the reports for subscribed nodes.
        Map<InterfaceNode, Double> baseLoads = new HashMap<>();
        for (InterfaceNode node : nodes) {
            try {
                AppResponse<NodeReport> report = node.getReport();
                if (!report.isSuccess()) continue;
                NodeReport rep = report.getData();
                //Following partial equation: a*cpuUsage + b*activeTasks + c*queueLength
                double cost = 0.3 * rep.cpuUsage() + 0.1 * rep.activeTasks() + 0.2 * rep.queueLength();
                baseLoads.put(node, cost);
            } catch (Exception ignore) {}
        }
        if (baseLoads.isEmpty()) {
            storeMetadata(transactions, userId, time.toString());
            return new AppResponse<>(false, "All nodes unavailable", fileResponses);
        }

        //Initialize variables.
        List<T> toConvert = new ArrayList<>(Arrays.asList(files)); //Array of files converted to list.
        Map<T, List<AppResponse<Iteration>>> iterationHistory = new LinkedHashMap<>(); //A map of the files and the list of nodes iterations.
        for (T f : files) iterationHistory.put(f, new ArrayList<>()); //Populate map with files and empty arraylists.
        Map<T, FileResult> finalResults = new LinkedHashMap<>(); //For each original file, a FileResult.

        //Iterate for max 5 attempts for conversion and files to convert is not empty.
        for (int attempt = 1; attempt <= 5 && !toConvert.isEmpty(); attempt++) {
            //Map files to nodes (for dispatching) based on dynamic load.
            Map<InterfaceNode, List<T>> assignment = new HashMap<>();
            Map<InterfaceNode, Double> dynamicLoads = new HashMap<>(baseLoads); //For partial loads + file sizes.
            for (T file : toConvert) {
                long size = (file instanceof String)
                        ? 5000L //URL
                        : calculateOriginalSize(((OfficeFile) file).getFileBase64()); //Office
                long kb = size / 1000;

                //Sort value ASC and get the first entry key.
                InterfaceNode chosen = sortByValueAscending(dynamicLoads).entrySet().iterator().next().getKey();

                //Add the file to get dispatched for that chosen node.
                assignment.computeIfAbsent(chosen, k -> new ArrayList<>()).add(file);

                //Increase load for chosen node
                dynamicLoads.put(chosen, dynamicLoads.get(chosen) + 0.4 * kb);
            }

            //Dispatch files in parallel
            ExecutorService pool = Executors.newFixedThreadPool(nodes.size()); //Pool of threads based on nodes quantity.
            ConcurrentMap<T, FileResult> attemptResults = new ConcurrentHashMap<>(); //Concurrent map for thread-safe operations.

            //For every entry of the map, do the rightful dispatches.
            for (Map.Entry<InterfaceNode, List<T>> entry : assignment.entrySet()) {
                InterfaceNode node = entry.getKey();
                for (T file : entry.getValue()) {
                    int finalAttempt = attempt;

                    //Submit a runnable
                    pool.submit(() -> {
                        List<AppResponse<Iteration>> iters = new ArrayList<>(); //For iterations done within the conversion.
                        boolean success = false; //Success is false for starters.
                        File resultFile = File.empty(); //The file is empty for starters.
                        try {
                            //Dispatch depending on the strategy.
                            AppResponse<Map<File,Iteration>> dr = (strategy == 1)
                                    ? node.dispatchOffice(((OfficeFile) file).getFileBase64(), ((OfficeFile) file).getFileName())
                                    : node.dispatchURL((String) file);

                            //Make sure the data/response is not null/empty.
                            if (dr != null && dr.getData() != null && !dr.getData().isEmpty()) {
                                var ent = dr.getData().entrySet().iterator().next();
                                //Add to the iterations, the wrapper with its corresponding iteration.
                                iters.add(new AppResponse<>(dr.isSuccess(), dr.getMessage(), ent.getValue()));

                                //Finally, assign file and success bool.
                                resultFile = ent.getKey();
                                success = dr.isSuccess();
                            }
                        } catch (RemoteException re) {
                            //Set a default AppResponse, stating that an error occurred with its message for logging purposes.
                            iters.add(
                                    new AppResponse<>(
                                            false, re.getMessage(),
                                            new Iteration(Instant.now().toString(),
                                                    node.toString(), Instant.now().toString())
                                    )
                            );
                        }

                        //Set the success of the file conversion.
                        AppResponse<File> fileResp = new AppResponse<>(
                                success,
                                success ? "File converted successfully." : "Attempt " + finalAttempt + " failed.",
                                resultFile
                        );

                        //Add response to the concurrent map.
                        attemptResults.put(file, new FileResult(fileResp, iters));
                    });
                }
            }

            //Shutdown and terminate the thread pool
            pool.shutdown();
            try { pool.awaitTermination(1, TimeUnit.HOURS); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            //Gather the conversion results for next retry if applicable.
            List<T> nextRetry = new ArrayList<>();
            for (T file : toConvert) {
                FileResult fr = attemptResults.get(file);

                //Add the iterations done for the original file/url to convert.
                iterationHistory.get(file).addAll(fr.iterations);

                if (fr.fileResponse.isSuccess()) {
                    //Put it on the finalResults
                    finalResults.put(file, new FileResult(fr.fileResponse, iterationHistory.get(file)));
                } else {
                    //Add it for retry.
                    nextRetry.add(file);
                }
            }

            //Now files to convert, are the ones that failed.
            toConvert = nextRetry;
        }

        //Wrap failures after 5 unsuccessful attempts.
        for (T file : toConvert) {
            List<AppResponse<Iteration>> iters = iterationHistory.get(file);
            AppResponse<File> resp = new AppResponse<>(false,
                    "File conversion failed after 5 attempts.", File.empty());
            finalResults.put(file, new FileResult(resp, iters));
        }

        //Pipe data for transaction map
        for (T file : files) {
            FileResult fr = finalResults.get(file);
            fileResponses.add(fr.fileResponse);
            transactions.put(fr.fileResponse, fr.iterations);
        }

        //Store the metadata for the transaction, adding the timestamp when it was queried.
        storeMetadata(transactions, userId, time.toString());

        return new AppResponse<>(true, "Files processing complete.", fileResponses);
    }

    private long calculateOriginalSize(String base64String) {
        String sanitized = base64String.replaceAll("\\s+", ""); //Remove whitespaces

        //Calculate padding characters
        int padding = 0;
        if (sanitized.endsWith("==")) {
            padding = 2;
        } else if (sanitized.endsWith("=")) {
            padding = 1;
        }

        //Formula for calculating size
        return ((sanitized.length() * 3L / 4) - padding);
    }

    public List<InterfaceNode> getSubscribedNodes(){
        return Collections.unmodifiableList(nodes);
    }

    private <T> Map<InterfaceNode, List<T>> assignByCost(Map<InterfaceNode, Double> baseLoads, List<T> toConvert, int strategy) {
        Map<InterfaceNode, List<T>> assignment = new HashMap<>();
        Map<InterfaceNode, Double> dynamicLoads = new HashMap<>(baseLoads);
        for (T file : toConvert) {
            long size = (file instanceof String) ? 5000L : calculateOriginalSize(((OfficeFile) file).getFileBase64());
            long kb = size / 1000;

            // find node with minimal weighted cost
            InterfaceNode chosen = dynamicLoads.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElseThrow()
                    .getKey();

            assignment.computeIfAbsent(chosen, k -> new ArrayList<>()).add(file);
            // update load: weightCpu * cpu + weightActive * activeTasks + weightQueue * queueLength + weightFileSize * kb
            double newLoad = dynamicLoads.get(chosen)
                    + weightFileSize * kb;
            dynamicLoads.put(chosen, newLoad);
        }
        return assignment;
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // Ascending order by value
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void logWeightsAndLatency(long latencyMs) {
        System.out.printf(Locale.ROOT,
                "Weights[a=%.2f, b=%.2f, c=%.2f, d=%.2f] latency=%dms%n",
                weightCpu, weightActive, weightQueue, weightFileSize, latencyMs);
        // Optionally append to file as shown in tuner
    }
}
