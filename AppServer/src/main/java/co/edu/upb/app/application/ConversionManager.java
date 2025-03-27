package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;
import co.edu.upb.app.domain.models.AppResponse;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ConversionManager implements IConversionManager, InterfacePublisher {

    private IMetricsManager metricsManager;
    private ArrayList<InterfaceNode> nodes;

    public ConversionManager(IMetricsManager metricsManager){
        this.metricsManager = metricsManager;
        this.nodes = new ArrayList<>();
    }


    @Override
    public AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException {
        return null;
    }

    @Override
    public AppResponse<String[]> queueOfficeConversion(String[] files) {
        return null;
    }

    @Override
    public AppResponse<String[]> queueURLConversion(String[] files) {
        return null;
    }
}
