package co.edu.upb.app.factory;

import co.edu.upb.app.application.AppManager;
import co.edu.upb.app.application.AuthManager;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.application.MetricsManager;
import co.edu.upb.app.config.Environment;
import co.edu.upb.app.infrastructure.FBAuthServer;
import co.edu.upb.authServer.Interfaces.InterfaceAuth;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.NodeRegistry;
import co.edu.upb.app.infrastructure.StorageClient;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppFactory {

    private final ConversionManager conversionManager;
    private final MetricsManager metricsManager;
    private InterfaceAuth authStub;

    public AppFactory(){
        StorageClient storageServer = null;
        try {
            storageServer = new StorageClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.metricsManager = new MetricsManager(storageServer);
        try {
            this.conversionManager = new ConversionManager(metricsManager);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    //For getting the App Server instance (using SOAP)
    public AppServer getAppServerInstance() {
        String rmiUrl = "rmi://" + Environment.getInstance()
                .getDotenv()
                .get("AUTH_URL");

        try {
            authStub = (InterfaceAuth) Naming.lookup(rmiUrl);
        } catch (Exception e) {
            //A log to warn that is a fallback stub and not the real one!
            System.err.println("[WARN] Could not reach Auth Server at "
                    + rmiUrl + ": " + e.getMessage());

            //Use the fallback
            try {
                authStub = new FBAuthServer();
            } catch (RemoteException rex) {
                throw new RuntimeException("Failed to initialize FallbackAuth", rex);
            }

            //Schedule a reconnection
            scheduleAuthReconnector(rmiUrl);
        }

        AuthManager authManager = new AuthManager(authStub);
        AppManager  appManager  = new AppManager(authManager,
                conversionManager,
                metricsManager);

        return new AppServer(appManager);
    }

    //For getting the instance of the Node Registry (using RMI)
    public NodeRegistry getNodeRegistryInstance(){
        return new NodeRegistry(conversionManager);
    }

    private void scheduleAuthReconnector(String rmiUrl) {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
                    try {
                        //Swap the stub for the real one.
                        authStub = (InterfaceAuth) Naming.lookup(rmiUrl);
                        System.out.println("[INFO] Reconnected to Auth Server");
                        scheduler.shutdown();
                    } catch (Exception ignore) {
                        //No exception thrown due to unsuccessful connection
                    }
                }, 30, //Initial of 30 seconds
                60, //Reconnect every 60 seconds.
                TimeUnit.SECONDS);
    }
}
