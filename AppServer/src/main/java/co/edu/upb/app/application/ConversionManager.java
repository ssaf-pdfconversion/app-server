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
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ConversionManager extends UnicastRemoteObject implements IConversionManager, InterfacePublisher {

    private final IMetricsManager metricsManager;
    private final ArrayList<InterfaceNode> nodes;

    public ConversionManager(IMetricsManager metricsManager) throws RemoteException{
        this.metricsManager = metricsManager;
        this.nodes = new ArrayList<>();
    }

    @Override
    public AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException {
        this.nodes.add(node);
        return new AppResponse<Boolean>(true, "Node succesfully subscribed", true);
    }

    @Override
    public AppResponse<ConvertedFile[]> queueOfficeConversion(OfficeFile[] files, Integer userId) {
        try{
            //FIRST: We queue the conversion using the load balancing algorithm
            //In here, strategy 1 means we're doing an Office conversion. The generic type is gonna be an OfficeFile.
            AppResponse<List<AppResponse<File>>> response = queueAlgorithm(files, 1, userId);

            ConvertedFile[] convertedFilesArray = getConvertedFiles(response);

            return new AppResponse<>(true, response.getMessage(), convertedFilesArray);
        } catch (Exception e) {
            e.printStackTrace();
            return new AppResponse<>(false, "Office files couldn't be converted", new ConvertedFile[0]);
        }
    }

    @Override
    public AppResponse<ConvertedFile[]> queueURLConversion(String[] files, Integer userId) {
        try{
            //FIRST: We queue the conversion using the load balancing algorithm
            //In here, strategy 2 means we're doing a URL conversion. The generic type is gonna be a String.
            AppResponse<List<AppResponse<File>>> response = queueAlgorithm(files, 2, userId);

            ConvertedFile[] convertedFilesArray = getConvertedFiles(response);

            return new AppResponse<>(true, response.getMessage(), convertedFilesArray);
        } catch (Exception e) {
            e.printStackTrace();
            return new AppResponse<>(false, "URLs couldn't be converted", new ConvertedFile[0]);
        }
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

        AppResponse<Boolean> response = this.metricsManager.storeMetadata(new Transaction(
                timestampQuery, conversions.toArray(new Conversion[0])
        ));

        System.out.println("Storage server with success on "+response.isSuccess() + " with message " + response.getMessage());
    }

    private <T> AppResponse<List<AppResponse<File>>> queueAlgorithm(T[] files, int strategy, int userId) {
        List<AppResponse<File>> fileResponses = new ArrayList<>();
        Map<AppResponse<File>, List<AppResponse<Iteration>>> transactions = new HashMap<>();
        Instant time = Instant.now();

        if (nodes.isEmpty()) {
            storeMetadata(transactions, userId, time.toString());
            return new AppResponse<>(true, "No resources available for converting", fileResponses);
        }

        // 1) Fetch base loads once
        Map<InterfaceNode, Double> baseLoads = new HashMap<>();
        for (InterfaceNode node : nodes) {
            try {
                AppResponse<NodeReport> report = node.getReport();
                if (!report.isSuccess()) continue;
                NodeReport rep = report.getData();
                double cost = 0.3 * rep.cpuUsage() + 0.1 * rep.activeTasks() + 0.2 * rep.queueLength();
                baseLoads.put(node, cost);
            } catch (Exception ignore) {}
        }
        if (baseLoads.isEmpty()) {
            storeMetadata(transactions, userId, time.toString());
            return new AppResponse<>(false, "All nodes unavailable", fileResponses);
        }

        // Prepare retry list and history
        List<T> toConvert = new ArrayList<>(Arrays.asList(files));
        Map<T, List<AppResponse<Iteration>>> iterationHistory = new LinkedHashMap<>();
        for (T f : files) iterationHistory.put(f, new ArrayList<>());
        Map<T, FileResult> finalResults = new LinkedHashMap<>();
        Random rand = new Random();

        // Retry up to 5 times
        for (int attempt = 1; attempt <= 5 && !toConvert.isEmpty(); attempt++) {
            // 2) Assignment phase: map files to nodes based on dynamic load
            Map<InterfaceNode, List<T>> assignment = new HashMap<>();
            Map<InterfaceNode, Double> dynamicLoads = new HashMap<>(baseLoads);
            for (T file : toConvert) {
                long size = (file instanceof String)
                        ? 5000L
                        : calculateOriginalSize(((OfficeFile) file).getFileBase64());
                long kb = size / 1000;

                // find minimum score
                InterfaceNode chosen = sortByValueAscending(dynamicLoads).entrySet().iterator().next().getKey();

                assignment.computeIfAbsent(chosen, k -> new ArrayList<>()).add(file);
                // increase load for chosen node
                dynamicLoads.put(chosen, dynamicLoads.get(chosen) + 0.4 * kb);
            }

            // 3) Dispatch phase: fire-and-forget in parallel
            ExecutorService pool = Executors.newCachedThreadPool();
            ConcurrentMap<T, FileResult> attemptResults = new ConcurrentHashMap<>();

            for (Map.Entry<InterfaceNode, List<T>> entry : assignment.entrySet()) {
                InterfaceNode node = entry.getKey();
                for (T file : entry.getValue()) {
                    int finalAttempt = attempt;
                    pool.submit(() -> {
                        List<AppResponse<Iteration>> iters = new ArrayList<>();
                        boolean success = false;
                        File resultFile = File.empty();
                        try {
                            AppResponse<Map<File,Iteration>> dr = (strategy == 1)
                                    ? node.dispatchOffice(((OfficeFile) file).getFileBase64(), ((OfficeFile) file).getFileName())
                                    : node.dispatchURL((String) file);
                            if (dr != null && dr.getData() != null && !dr.getData().isEmpty()) {
                                var ent = dr.getData().entrySet().iterator().next();
                                iters.add(new AppResponse<>(dr.isSuccess(), dr.getMessage(), ent.getValue()));
                                resultFile = ent.getKey();
                                success = dr.isSuccess();
                            }
                        } catch (RemoteException re) {
                            iters.add(new AppResponse<>(false, re.getMessage(),
                                    new Iteration(Instant.now().toString(), node.toString(), Instant.now().toString())));
                        }
                        AppResponse<File> fileResp = new AppResponse<>(
                                success,
                                success ? "File converted successfully." : "Attempt " + finalAttempt + " failed.",
                                resultFile
                        );
                        attemptResults.put(file, new FileResult(fileResp, iters));
                    });
                }
            }
            pool.shutdown();
            try { pool.awaitTermination(1, TimeUnit.HOURS); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            // 4) Collect results and prepare next retry list
            List<T> nextRetry = new ArrayList<>();
            for (T file : toConvert) {
                FileResult fr = attemptResults.get(file);
                iterationHistory.get(file).addAll(fr.iterations);
                if (fr.fileResponse.isSuccess()) {
                    finalResults.put(file, new FileResult(fr.fileResponse, iterationHistory.get(file)));
                } else {
                    nextRetry.add(file);
                }
            }
            toConvert = nextRetry;
        }

        // 5) Finalize failures after 5 attempts
        for (T file : toConvert) {
            List<AppResponse<Iteration>> iters = iterationHistory.get(file);
            AppResponse<File> resp = new AppResponse<>(false,
                    "File conversion failed after 5 attempts.", File.empty());
            finalResults.put(file, new FileResult(resp, iters));
        }

        // 6) Flatten responses preserving order
        for (T file : files) {
            FileResult fr = finalResults.get(file);
            fileResponses.add(fr.fileResponse);
            transactions.put(fr.fileResponse, fr.iterations);
        }

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
}
