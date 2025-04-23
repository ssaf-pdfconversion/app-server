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
        Transaction generalTransaction = new Transaction(timestampQuery, new Conversion[0]);
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

        this.metricsManager.storeMetadata(new Transaction(
                timestampQuery, conversions.toArray(new Conversion[0])
        ));
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
                double cost = 0.1 * rep.cpuUsage() + 0.2 * rep.activeTasks() + 0.3 * rep.queueLength();
                baseLoads.put(node, cost);
            } catch (Exception ignore) {}
        }
        if (baseLoads.isEmpty()) {
            storeMetadata(transactions, userId, time.toString());
            return new AppResponse<>(false, "All nodes unavailable", fileResponses);
        }

        // 2) Prepare retry list and history, preserving input order
        List<T> toConvert = new ArrayList<>(Arrays.asList(files));
        Map<T, List<AppResponse<Iteration>>> iterationHistory = new LinkedHashMap<>();
        for (T f : files) iterationHistory.put(f, new ArrayList<>());
        Map<T, FileResult> finalResults = new LinkedHashMap<>();
        Random rand = new Random();

        // 3) Sequential dispatch, retry up to 5 times for failures
        for (int attempt = 1; attempt <= 5 && !toConvert.isEmpty(); attempt++) {
            List<T> nextRetry = new ArrayList<>();
            for (T file : toConvert) {
                // calculate normalized size
                long size = (file instanceof String)
                        ? 5000L
                        : calculateOriginalSize(((OfficeFile) file).getFileBase64());
                long kb = size / 1000;

                // pick best node based on current baseLoads + size factor
                double minScore = baseLoads.entrySet().stream()
                        .mapToDouble(e -> e.getValue() + 0.4 * kb)
                        .min().orElse(Double.MAX_VALUE);
                List<InterfaceNode> bestNodes = baseLoads.entrySet().stream()
                        .filter(e -> Math.abs(e.getValue() + 0.4 * kb - minScore) < 1e-6)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                InterfaceNode chosen = bestNodes.size() > 1
                        ? bestNodes.get(rand.nextInt(bestNodes.size()))
                        : bestNodes.get(0);

                // dispatch synchronously
                List<AppResponse<Iteration>> iters = new ArrayList<>();
                boolean success = false;
                File resultFile = File.empty();
                try {
                    AppResponse<Map<File, Iteration>> dr = (strategy == 1)
                            ? chosen.dispatchOffice(((OfficeFile) file).getFileBase64(), ((OfficeFile) file).getFileName())
                            : chosen.dispatchURL((String) file);
                    if (dr != null && dr.getData() != null && !dr.getData().isEmpty()) {
                        var entry = dr.getData().entrySet().iterator().next();
                        iters.add(new AppResponse<>(dr.isSuccess(), dr.getMessage(), entry.getValue()));
                        resultFile = entry.getKey();
                        success = dr.isSuccess();
                    }
                } catch (RemoteException re) {
                    iters.add(new AppResponse<>(false, re.getMessage(),
                            new Iteration(Instant.now().toString(), chosen.toString(), Instant.now().toString())));
                }

                // record iterations
                iterationHistory.get(file).addAll(iters);

                // update load on chosen node
                baseLoads.put(chosen, baseLoads.get(chosen) + 0.4 * kb);

                // record success or queue for retry
                if (success) {
                    finalResults.put(file, new FileResult(
                            new AppResponse<>(true, "File converted successfully.", resultFile),
                            iterationHistory.get(file)
                    ));
                } else {
                    nextRetry.add(file);
                }
            }
            toConvert = nextRetry;
        }

        // 4) After 5 attempts, mark any remaining as failed
        for (T file : toConvert) {
            List<AppResponse<Iteration>> iters = iterationHistory.get(file);
            AppResponse<File> resp = new AppResponse<>(false,
                    "File conversion failed after 5 attempts.", File.empty());
            finalResults.put(file, new FileResult(resp, iters));
        }

        // 5) Flatten results preserving original order
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
