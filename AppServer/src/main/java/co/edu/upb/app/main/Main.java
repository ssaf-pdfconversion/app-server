package co.edu.upb.app.main;

import co.edu.upb.app.factory.AppFactory;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.NodeRegistry;

public class Main {
    public static void main(String[] args) {

        AppFactory appFactory = new AppFactory();

        //Running SOAP server
        AppServer appServer;
        appServer = appFactory.getAppServerInstance();

        appServer.run();

        //Running Node registry server
        NodeRegistry nodeRegistry = appFactory.getNodeRegistryInstance();
        nodeRegistry.run();
    }
}
