package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ConversionManager extends UnicastRemoteObject implements IConversionManager, InterfacePublisher {

    private final IMetricsManager metricsManager;
    private final ArrayList<InterfaceNode> nodes;
    private final Map<InterfaceNode, Double> fullNodes;
    private final DateTimeFormatter formatter;

    public ConversionManager(IMetricsManager metricsManager) throws RemoteException{
        this.metricsManager = metricsManager;
        this.nodes = new ArrayList<>();
        this.fullNodes = new HashMap<>();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
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

            return new AppResponse<>(true, "Office files converted successfully", convertedFilesArray);
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

            return new AppResponse<>(true, "URLs converted successfully", convertedFilesArray);
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
        // now maps each file‐response to its list of iteration‐responses
        Map<AppResponse<File>, List<AppResponse<Iteration>>> transactions = new HashMap<>();

        // capture the overall transaction timestampQuery once
        String transactionTimestamp = ZonedDateTime.now().format(formatter);

        try {
            for (T file : files) {
                // collect each iteration as an AppResponse<Iteration>
                List<AppResponse<Iteration>> iterationList = new ArrayList<>();
                File finalFileResult = null;
                boolean conversionSuccess = false;

                for (int attempt = 1; attempt <= 5 && !conversionSuccess; attempt++) {
                    // Update node metrics to calculate the partial load for every node.
                    fullNodes.clear();
                    for (InterfaceNode node : nodes) {
                        try {
                            AppResponse<NodeReport> response = node.getReport();

                            if (!response.isSuccess()) {
                                throw new Exception(response.getMessage());
                            }

                            NodeReport report = response.getData();

                            double partialCalc = 0.1 * report.cpuUsage()
                                    + 0.2 * report.activeTasks()
                                    + 0.3 * report.queueLength();
                            fullNodes.put(node, partialCalc);
                        } catch (Exception e) {
                            //Remove the node from the full node, due to any sort of exceptions
                            fullNodes.remove(node);
                        }
                    }

                    // Adjust each node's calculation by adding the normalized file size.
                    long size = 0L;

                    if (file instanceof String) {
                        size = 5000; //For URLs, an average size of 5000 bytes is given.
                    } else if (file instanceof OfficeFile) {
                        size = calculateOriginalSize(((OfficeFile) file).getFileBase64()); //For office files, the size is calculated based on its data encoded on a base64 string.
                    }

                    long normalizedSize = size / 1000;  // Normalization factor as needed.
                    Map<InterfaceNode, Double> fullCalculations = new HashMap<>();
                    for (Map.Entry<InterfaceNode, Double> entry : fullNodes.entrySet()) {
                        double fullCalc = entry.getValue() + (0.4 * normalizedSize);
                        fullCalculations.put(entry.getKey(), fullCalc);
                    }
                    Map<InterfaceNode, Double> sortedNodes = sortByValueAscending(fullCalculations);
                    InterfaceNode bestNode = sortedNodes.entrySet().iterator().next().getKey(); // Best node to dispatch the conversion.

                    // Attempt dispatching the conversion.

                    try {
                        AppResponse<Map<File,Iteration>> dispatchResp;
                        if (strategy == 1 && file instanceof OfficeFile officeFile) {
                            dispatchResp = bestNode.dispatchOffice(officeFile.getFileBase64(), officeFile.getFileName());
                        } else {
                            dispatchResp = bestNode.dispatchURL((String) file);
                        }

                        if (dispatchResp != null && dispatchResp.getData() != null && !dispatchResp.getData().isEmpty()) {
                            Map.Entry<File,Iteration> entry = dispatchResp.getData().entrySet().iterator().next();
                            // wrap the raw Iteration into an AppResponse<Iteration>
                            AppResponse<Iteration> iterResp = new AppResponse<>(
                                    dispatchResp.isSuccess(),
                                    dispatchResp.getMessage(),
                                    entry.getValue()
                            );
                            iterationList.add(iterResp);

                            finalFileResult = entry.getKey();
                            conversionSuccess = dispatchResp.isSuccess();
                        }

                    } catch (RemoteException re) {
                        // on RPC failure, record a “failed” iteration
                        Iteration fallback = new Iteration(
                                ZonedDateTime.now().format(formatter),
                                bestNode.toString(),
                                ZonedDateTime.now().format(formatter)
                        );
                        iterationList.add(new AppResponse<>(
                                false,
                                re.getMessage(),
                                fallback
                        ));
                        finalFileResult = File.empty();
                    }
                }

                if (finalFileResult == null) {
                    finalFileResult = File.empty();
                }

                // build the per-file AppResponse<File>
                AppResponse<File> fileResp = new AppResponse<>(
                        conversionSuccess,
                        conversionSuccess
                                ? "File converted successfully."
                                : "File conversion failed after 5 attempts.",
                        finalFileResult
                );

                fileResponses.add(fileResp);
                transactions.put(fileResp, iterationList);
            }

            this.storeMetadata(transactions, userId, transactionTimestamp);

            return new AppResponse<>(true, "All files were converted successfully!", fileResponses);

        } catch (Exception e) {
            return new AppResponse<>(false, "Files couldn't be converted", new ArrayList<>());
        }
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
