package co.edu.upb.app.factory;

import co.edu.upb.app.application.AppManager;
import co.edu.upb.app.application.AuthManager;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.application.MetricsManager;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.AuthServer;
import co.edu.upb.app.infrastructure.NodeRegistry;
import co.edu.upb.app.infrastructure.StorageServer;

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
        AuthServer authServer = new AuthServer();
        AuthManager authManager = new AuthManager(authServer);

        AppManager appManager = new AppManager(authManager, conversionManager, metricsManager);

        return new AppServer(appManager);

    }

    //For getting the instance of the Node Registry (using RMI)
    public NodeRegistry getNodeRegistryInstance(){
        return new NodeRegistry(conversionManager);
    }
}
