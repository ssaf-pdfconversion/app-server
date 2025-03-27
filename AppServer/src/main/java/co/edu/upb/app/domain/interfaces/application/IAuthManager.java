package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.AppResponse;

public interface IAuthManager {
    public AppResponse<String> register(String username, String password, String nombre, String apellido, String email);
    public AppResponse<String>  login(String username, String password);
    public AppResponse<Boolean>  validateJWT(String JWT);
}
