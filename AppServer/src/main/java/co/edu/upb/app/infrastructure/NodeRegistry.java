package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfacePublisher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class NodeRegistry {

    private final String port;
    private final String serverIp;
    private final InterfacePublisher service;

    public NodeRegistry(InterfacePublisher conversionManager) {
        this.port = Environment.getInstance().getDotenv().get("REGISTRY_PORT");
        this.serverIp = Environment.getInstance().getDotenv().get("REGISTRY_IP");
        this.service = conversionManager;
    }

    public void run() {
        try {
            System.setProperty("java.rmi.server.hostname", serverIp);

            LocateRegistry.createRegistry(Integer.parseInt(port));
            try {
                Naming.rebind("//127.0.0.1:"+port+"/registry", service);
            } catch (RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
