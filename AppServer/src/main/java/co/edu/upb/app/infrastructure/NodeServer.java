package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceNode;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.Conversion;
import co.edu.upb.app.domain.models.File;
import co.edu.upb.app.domain.models.NodeReport;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class NodeServer implements InterfaceNode {

    private final InterfaceNode service;

    public NodeServer () {
        try {
            this.service = (InterfaceNode) Naming.lookup("rmi" + Environment.getInstance().getDotenv().get("NODE_URL"));
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse<File> dispatchOffice(Conversion file) throws RemoteException {
        return null;
    }

    @Override
    public AppResponse<File> dispatchURL(Conversion url) throws RemoteException {
        return null;
    }

    @Override
    public AppResponse<NodeReport> getReport() throws RemoteException {
        return null;
    }
}
