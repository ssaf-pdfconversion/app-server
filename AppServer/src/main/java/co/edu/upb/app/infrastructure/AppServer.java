package co.edu.upb.app.infrastructure;

import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import jakarta.xml.ws.Endpoint;

public class AppServer {

    private final InterfaceApp service;

    public AppServer(InterfaceApp service){
        this.service = service;
    }

    public void run() {
        try {
            Endpoint.publish("http://localhost:5000/appserver", service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
