package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceAuth;
import co.edu.upb.app.domain.models.AppResponse;

public class AuthManager implements IAuthManager {

    private final InterfaceAuth auth;

    public AuthManager(InterfaceAuth auth){
        this.auth = auth;
    }

    @Override
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email) {
        return null;
    }

    @Override
    public AppResponse<String>  login(String username, String password){
        return null;
    }

    @Override
    public AppResponse<Boolean>  validateJWT(String JWT) {
        return null;
    }
}
