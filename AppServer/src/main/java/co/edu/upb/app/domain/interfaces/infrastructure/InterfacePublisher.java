package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacePublisher extends Remote {
    AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException;
}
