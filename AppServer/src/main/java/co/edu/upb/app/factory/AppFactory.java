package co.edu.upb.app.factory;

import co.edu.upb.app.application.AppManager;
import co.edu.upb.app.application.AuthManager;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.application.MetricsManager;
import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceAuth;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.NodeRegistry;
import co.edu.upb.app.infrastructure.StorageServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class AppFactory {

    private final ConversionManager conversionManager;
    private final MetricsManager metricsManager;

    public AppFactory(){
        StorageServer storageServer = new StorageServer();
        this.metricsManager = new MetricsManager(storageServer);
        this.conversionManager = new ConversionManager(metricsManager);
    }

    //For getting the App Server instance (using SOAP)
    public AppServer getAppServerInstance(){

        //Look for the implementation of the RMI registry of the Auth Server.
        InterfaceAuth auth;
        try {
            auth = (InterfaceAuth) Naming.lookup("rmi://" + Environment.getInstance().getDotenv().get("AUTH_URL"));
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
