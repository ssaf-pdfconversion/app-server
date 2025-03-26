package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.AppResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacePublisher extends Remote {
    AppResponse<Boolean> subscribeNode(InterfaceNode node) throws RemoteException;
}
