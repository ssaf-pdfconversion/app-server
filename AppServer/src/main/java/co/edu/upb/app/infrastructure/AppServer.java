package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class AppServer {

    private final InterfaceApp service;

    public AppServer(InterfaceApp service){
        this.service = service;
    }

    public void run() {
        try {
            String ksPath = Environment.getInstance().getDotenv().get("SSL_PATH");
            char[] ksPass = Environment.getInstance().getDotenv().get("SSL_PASS").toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(ksPath)) {
                ks.load(fis, ksPass);
            }

            //Setup the keymanager and trustmanager
            KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, ksPass);

            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);  // trust the same certs

            //SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            //Create the HTTP server based on
            int port = Integer.parseInt(Environment.getInstance().getDotenv().get("SOAP_PORT"));
            HttpsServer httpsServer = HttpsServer.create(
                    new InetSocketAddress(port), 0
            );
            HttpContext ctx = httpsServer.createContext("/soap");
            Endpoint endpoint = Endpoint.create(service);
            endpoint.publish(ctx);

            //Start server
            httpsServer.start();
            System.out.println("App server running on port " + Environment.getInstance().getDotenv().get("SOAP_PORT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
