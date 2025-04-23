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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ConversionManager extends UnicastRemoteObject implements IConversionManager, InterfacePublisher {

    private final IMetricsManager metricsManager;
    private final ArrayList<InterfaceNode> nodes;
    private final Map<InterfaceNode, Double> fullNodes;

    public ConversionManager(IMetricsManager metricsManager) throws RemoteException{
        this.metricsManager = metricsManager;
        this.nodes = new ArrayList<>();
        this.fullNodes = new HashMap<>();
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

        // 1) create a thread-pool
        ExecutorService executor = Executors.newFixedThreadPool(nodes.size());

        // 2) kick off one future per file
        List<CompletableFuture<FileResult>> futures = new ArrayList<>();
        for (T file : files) {
            CompletableFuture<FileResult> cf = CompletableFuture.supplyAsync(() -> {
                List<AppResponse<Iteration>> iterationList = new ArrayList<>();
                File finalFileResult = null;
                boolean conversionSuccess = false;

                // up to 5 attempts just like before
                for (int attempt = 1; attempt <= 5 && !conversionSuccess; attempt++) {
                    // — recompute load on each node —
                    fullNodes.clear();
                    for (InterfaceNode node : nodes) {
                        try {
                            AppResponse<NodeReport> r = node.getReport();
                            if (!r.isSuccess()) throw new Exception(r.getMessage());
                            NodeReport rep = r.getData();
                            double cost = 0.1*rep.cpuUsage()
                                    + 0.2*rep.activeTasks()
                                    + 0.3*rep.queueLength();
                            fullNodes.put(node, cost);
                        } catch (Exception e) {
                            fullNodes.remove(node);
                        }
                    }

                    // — normalize file size —
                    long size = (file instanceof String)
                            ? 5000
                            : calculateOriginalSize(((OfficeFile)file).getFileBase64());
                    long normalized = size / 1000;

                    // — pick best node —
                    Map<InterfaceNode, Double> scores = fullNodes.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue() + 0.4 * normalized
                            ));
                    InterfaceNode best = sortByValueAscending(scores).keySet().iterator().next();

                    // — dispatch —
                    try {
                        AppResponse<Map<File,Iteration>> dr;
                        if (strategy == 1) {
                            OfficeFile of = (OfficeFile) file;
                            dr = best.dispatchOffice(of.getFileBase64(), of.getFileName());
                        } else {
                            dr = best.dispatchURL((String) file);
                        }
                        if (dr != null && dr.getData() != null && !dr.getData().isEmpty()) {
                            var entry = dr.getData().entrySet().iterator().next();
                            AppResponse<Iteration> ir = new AppResponse<>(
                                    dr.isSuccess(), dr.getMessage(), entry.getValue());
                            iterationList.add(ir);
                            finalFileResult = entry.getKey();
                            conversionSuccess = dr.isSuccess();
                        }
                    } catch (RemoteException re) {
                        // record a failed iteration
                        Iteration fb = new Iteration(Instant.now().toString(),
                                best.toString(),
                                Instant.now().toString());
                        iterationList.add(new AppResponse<>(false, re.getMessage(), fb));
                        finalFileResult = File.empty();
                    }
                }

                if (finalFileResult == null) finalFileResult = File.empty();

                AppResponse<File> fileResp = new AppResponse<>(
                        conversionSuccess,
                        conversionSuccess
                                ? "File converted successfully."
                                : "File conversion failed after 5 attempts.",
                        finalFileResult);

                return new FileResult(fileResp, iterationList);
            }, executor);

            futures.add(cf);
        }

        // 3) wait for all to finish
        List<FileResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        executor.shutdown();

        // 4) flatten back into your existing lists/maps
        for (FileResult r : results) {
            fileResponses.add(r.fileResponse);
            transactions.put(r.fileResponse, r.iterations);
        }

        // 5) store metrics and return
        storeMetadata(transactions, userId, time.toString());
        return new AppResponse<>(true, "All files were converted successfully!", fileResponses);
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
