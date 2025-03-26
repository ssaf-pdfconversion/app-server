package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceAuth;
import co.edu.upb.app.domain.models.AppResponse;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class AuthServer implements InterfaceAuth {

    private final InterfaceAuth service;

    public AuthServer () {
        try {
            this.service = (InterfaceAuth) Naming.lookup("rmi://" + Environment.getInstance().getDotenv().get("AUTH_URL"));
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email) throws RemoteException {
        return null;
    }

    @Override
    public AppResponse<String> login(String username, String password) throws RemoteException {
        return null;
    }

    @Override
    public AppResponse<String> validateJWT(String JWT) throws RemoteException {
        return null;
    }
}
