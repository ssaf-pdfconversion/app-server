package co.edu.upb.app.application;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.infrastructure.StorageServer;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ConversionManager implements IConversionManager, InterfacePublisher {

    private IMetricsManager metricsManager;
    private ArrayList<InterfaceNode> nodes;

    public ConversionManager(IMetricsManager metricsManager){
        this.metricsManager = metricsManager;
    }


    @Override
    public String[] queueOfficeConversion(String[] files) {
        return new String[0];
    }

    @Override
    public String[] queueURLConversion(String[] files) {
        return new String[0];
    }

    @Override
    public AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException {

        nodes.add(node);

        return null;
    }
}
