package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.node.domain.models.Conversion;
import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.node.domain.models.File;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
    public AppResponse<String[]> queueOfficeConversion(String[] files) {

        AppResponse<String[]> finalResponse = new AppResponse<>(false, "No se pudo convertir", new String[0]);

        for (InterfaceNode node : nodes) {
            try {
                 AppResponse<File> appResponse = node.dispatchOffice(new Conversion(0, files[0], false));
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
                AppResponse<File> appResponse = node.dispatchURL(new Conversion(0, files[0], false));
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
}
