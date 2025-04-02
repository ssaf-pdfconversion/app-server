package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import jakarta.xml.ws.Endpoint;

public class AppServer {

    private final InterfaceApp service;

    public AppServer(InterfaceApp service){
        this.service = service;
    }

    public void run() {
        try {
            Endpoint.publish(Environment.getInstance().getDotenv().get("SOAP_URL") +"/appserver", service);
            System.out.println("App server running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
