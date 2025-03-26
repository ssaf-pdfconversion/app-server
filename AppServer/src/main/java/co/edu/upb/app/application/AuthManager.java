package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceAuth;

public class AuthManager implements IAuthManager {

    private final InterfaceAuth auth;

    public AuthManager(InterfaceAuth auth){
        this.auth = auth;
    }

    @Override
    public String register(String username, String password, String nombre, String apellido, String email) {
        return null;
    }

    @Override
    public String login(String username, String password){
        return null;
    }

    @Override
    public Boolean validateJWT(String JWT) {
        return null;
    }
}
