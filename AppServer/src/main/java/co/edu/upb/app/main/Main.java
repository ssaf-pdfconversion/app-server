package co.edu.upb.app.main;

import co.edu.upb.app.application.AppManager;
import co.edu.upb.app.factory.AppFactory;
import co.edu.upb.app.infrastructure.AppServer;
import co.edu.upb.app.infrastructure.NodeRegistry;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) {

        AppFactory appFactory = new AppFactory();

        //Running SOAP server
        AppServer appServer = appFactory.getAppServerInstance();
        appServer.run();

        //Running Node registry server
        NodeRegistry nodeRegistry = appFactory.getNodeRegistryInstance();
        nodeRegistry.run();
    }
}
