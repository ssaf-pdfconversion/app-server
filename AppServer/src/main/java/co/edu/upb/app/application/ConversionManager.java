package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.Conversion;
import co.edu.upb.app.domain.models.File;
import co.edu.upb.app.domain.models.Metadata;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ConversionManager implements IConversionManager, InterfacePublisher {

    private final IMetricsManager metricsManager;
    private final ArrayList<InterfaceNode> nodes;

    public ConversionManager(IMetricsManager metricsManager){
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
        for (InterfaceNode node : nodes) {
            try {
                node.dispatchOffice(new Conversion(0, files[0], false));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        //TODO: Implement load balancing algorithm

        this.storeMetadata(0, 0, 0.0);

        return new AppResponse<String[]>(true, "Enviada conversión office", new String[0]);
    }

    @Override
    public AppResponse<String[]> queueURLConversion(String[] files) {
        for (InterfaceNode node : nodes) {
            try {
                node.dispatchURL(new Conversion(0, files[0], false));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        //TODO: Implement load balancing algorithm

        this.storeMetadata(0, 0, 0.0);

        return new AppResponse<String[]>(true, "Enviada conversión URL", new String[0]);
    }

    private void storeMetadata(Integer userId, Integer fileTypeId, Double size) {
        //TODO: Change this storeMetadata implementation
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String timestamp = now.format(formatter);

        this.metricsManager.storeMetadata(new Metadata(userId, fileTypeId, size, timestamp));
    }
}
