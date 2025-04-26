package co.edu.upb.app.infrastructure;

import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.authServer.Interfaces.InterfaceAuth;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FBAuthServer extends UnicastRemoteObject implements InterfaceAuth {
    public FBAuthServer() throws RemoteException {
    }

    @Override
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email) throws RemoteException {
        return new AppResponse<>(false, "No authentication available", "");
    }

    @Override
    public AppResponse<String> login(String username, String password) throws RemoteException {
        return new AppResponse<>(false, "No authentication available", "");
    }

    @Override
    public AppResponse<Boolean> validateJWT(String JWT) throws RemoteException {
        return new AppResponse<>(false, "No authentication available", false);
    }
}
