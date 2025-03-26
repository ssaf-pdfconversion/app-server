package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.AppResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceAuth extends Remote {
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email) throws RemoteException;
    public AppResponse<String> login(String username, String password) throws RemoteException;
    public AppResponse<String> validateJWT(String JWT) throws RemoteException;
}
