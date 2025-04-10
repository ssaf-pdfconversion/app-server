package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.node.domain.models.Conversion;
import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.node.domain.models.File;
import co.edu.upb.node.domain.models.NodeReport;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

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
    public AppResponse<String[]> queueOfficeConversion(String[] files) {

        AppResponse<String[]> finalResponse = new AppResponse<>(false, "No se pudo convertir", new String[0]);

        for (InterfaceNode node : nodes) {
            try {
                 AppResponse<File> appResponse = node.dispatchOffice(files[0]);
                 finalResponse = new AppResponse<>(false, appResponse.getMessage(), new String[0]);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        //TODO: Implement load balancing algorithm

        this.storeMetadata(0, 0, 0.0);

        return finalResponse;
    }

    @Override
    public AppResponse<String[]> queueURLConversion(String[] files) {
        AppResponse<String[]> finalResponse = new AppResponse<>(false, "No se pudo convertir", new String[0]);

        for (InterfaceNode node : nodes) {
            try {
                AppResponse<File> appResponse = node.dispatchURL(files[0]);
                finalResponse = new AppResponse<>(false, appResponse.getMessage(), new String[0]);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        //TODO: Implement load balancing algorithm

        this.storeMetadata(0, 0, 0.0);

        return finalResponse;
    }

    private void storeMetadata(Integer userId, Integer fileTypeId, Double size) {
        //TODO: Change this storeMetadata implementation
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String timestamp = now.format(formatter);

        this.metricsManager.storeMetadata(new Metadata(userId, fileTypeId, size, timestamp));
    }

//    private AppResponse<File[]> queueAlgorithm(String[] files, int strategy, int iterations){
//
//        //Attributes declaration
//        AppResponse<String[]> conversionResponse;
//        ArrayList<String> failedFiles = new ArrayList<>();
//
//        if(iterations >= 10){
//            return
//        }
//
//        //Timestamp
//        ZonedDateTime zonedDateTime = ZonedDateTime.now();
//        String timestamp = zonedDateTime.toString();
//
//        //Get node reports for every node and then put them on the map.
//        for (InterfaceNode node : nodes) {
//            try {
//                //Get actual node report
//                NodeReport report = node.getReport();
//
//                Double cpuUsage = report.cpuUsage();
//                Integer activeTasks = report.activeTasks();
//                Integer queueLength = report.queueLength();
//
//                //Calculate partial formula: a*cpuUsage + b*activeTasks + c*queueLength and add to map.
//                Double partialCalculation = 0.1*cpuUsage + 0.2*activeTasks + 0.3*queueLength;
//                this.fullNodes.put(node, partialCalculation);
//            } catch (RemoteException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        //For every file, assign a node
//        for (String file: files){
//            //Calculate the best node based on the formula: a*cpuUsage + b*activeTasks + c*queueLength + d*normalizedFileSize
//            long size = calculateOriginalSize(file);
//            long normalizedSize = size/1000;
//            Map<InterfaceNode, Double> fullCalculations = new HashMap<>();
//
//            for (Map.Entry<InterfaceNode, Double> entry : fullNodes.entrySet()){
//                //Get partial calculation for node report
//                Double calculation = entry.getValue();
//
//                //Add the d*normalizedFileSize
//                calculation+=0.4*normalizedSize;
//
//                fullCalculations.put(entry.getKey(), calculation);
//            }
//
//            AppResponse<File> nodeResponse;
//
//            //The first entry
//            Map<InterfaceNode, Double> firstEntry = sortByValueAscending(fullCalculations);
//
//            //Its key is the best node to queue
//            InterfaceNode bestNode = firstEntry.entrySet().iterator().next().getKey();
//
//            try {
//                if(strategy == 1){
//                    nodeResponse = bestNode.dispatchOffice(file);
//
//                    //Add the value of normalized size as a predicted value to the best node entry value.
//                    firstEntry.entrySet().iterator().next().setValue(firstEntry.entrySet().iterator().next().getValue() + 0.4*normalizedSize);
//                } else {
//                    nodeResponse = bestNode.dispatchURL(file);
//                }
//            } catch (RemoteException e) {
//                failedFiles.add(file);
//            }
//
//        }
//
//    }

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
