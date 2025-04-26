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
            String address = Environment.getInstance().getDotenv().get("SOAP_URL") +"/appserver";
            Endpoint.publish(address, service);
            System.out.println("App server running on "+address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
