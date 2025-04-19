package co.edu.upb.node.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.node.domain.models.File;
import co.edu.upb.node.domain.models.Iteration;
import co.edu.upb.node.domain.models.NodeReport;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface InterfaceNode extends Remote {
    public AppResponse<Map<File, Iteration>> dispatchOffice(String file, String originalFilename) throws RemoteException;
    public AppResponse<Map<File, Iteration>> dispatchURL(String url) throws RemoteException;
    public AppResponse<NodeReport> getReport() throws RemoteException;
}
