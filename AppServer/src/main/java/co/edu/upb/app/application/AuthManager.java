package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceAuth;
import co.edu.upb.app.domain.models.AppResponse;

import java.rmi.RemoteException;

public class AuthManager implements IAuthManager {

    private final InterfaceAuth auth;

    public AuthManager(InterfaceAuth auth){
        this.auth = auth;
    }

    @Override
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email) {
        try {
            return auth.register(username, password,nombre, apellido, email);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse<String>  login(String username, String password){
        try {
            return this.auth.login(username, password);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppResponse<Boolean>  validateJWT(String JWT) {
        try {
            return this.auth.validateJWT(JWT);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
