package co.edu.upb.app.domain.interfaces.application;

public interface IAuthManager {
    public String register(String username, String password, String nombre, String apellido, String email);
    public String login(String username, String password);
    public Boolean validateJWT(String JWT);
}
