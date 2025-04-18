package co.edu.upb.app.factory;

import co.edu.upb.app.application.AppManager;
import co.edu.upb.app.application.AuthManager;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.application.MetricsManager;
import co.edu.upb.app.config.Environment;
import co.edu.upb.authServer.Interfaces.InterfaceAuth;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.NodeRegistry;
import co.edu.upb.app.infrastructure.StorageClient;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class AppFactory {

    private final ConversionManager conversionManager;
    private final MetricsManager metricsManager;

    public AppFactory(){
        StorageClient storageServer = new StorageClient();
        this.metricsManager = new MetricsManager(storageServer);
        try {
            this.conversionManager = new ConversionManager(metricsManager);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    //For getting the App Server instance (using SOAP)
    public AppServer getAppServerInstance(){

        //Look for the implementation of the RMI registry of the Auth Server.
        InterfaceAuth auth;
        try {
            String name = "rmi://" + Environment.getInstance().getDotenv().get("AUTH_URL");
            System.out.println(name);
            auth = (InterfaceAuth) Naming.lookup(name);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
        AuthManager authManager = new AuthManager(auth);

        AppManager appManager = new AppManager(authManager, conversionManager, metricsManager);

        return new AppServer(appManager);

    }

    //For getting the instance of the Node Registry (using RMI)
    public NodeRegistry getNodeRegistryInstance(){
        return new NodeRegistry(conversionManager);
    }
}
