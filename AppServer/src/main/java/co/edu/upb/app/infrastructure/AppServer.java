package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
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

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ksPass);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            //SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            //Create the HTTP server
            int port = Integer.parseInt(Environment.getInstance().getDotenv().get("SOAP_PORT"));
            HttpsServer server = HttpsServer.create(
                    new InetSocketAddress(port), 0
            );

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    // grab the default SSL parameters from our context
                    SSLContext c = getSSLContext();
                    SSLParameters sslParams = c.getDefaultSSLParameters();
                    sslParams.setNeedClientAuth(false);
                    params.setSSLParameters(sslParams);
                }
            });

            //Create /appserver context and publish JAX-WS endpoint
            HttpContext ctx = server.createContext("/appserver");
            Endpoint endpoint = Endpoint.create(service);
            endpoint.publish(ctx);

            //Start the server
            server.setExecutor(null);
            server.start();
            System.out.println("App server running on port " + Environment.getInstance().getDotenv().get("SOAP_PORT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
