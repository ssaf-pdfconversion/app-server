package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.storage.Metadata;
import co.edu.upb.app.domain.models.storage.Transaction;
import com.google.gson.Gson;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class StorageClient implements InterfaceStorage {

    private final HttpClient client;
    private final String url;

    public StorageClient() throws Exception {
        this.url = Environment.getInstance().getDotenv().get("STORAGE_URL");

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        // 2) Initialize an SSLContext with it
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        // 3) Disable hostname verification (if needed)
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("");

        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(sc)
                .build();
    }

    @Override
    public HttpResponse<String> storeMetadata(Transaction data) {

        Gson gson = new Gson();
        String json = gson.toJson(new Metadata(data));

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url + "/storeMetadata"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            return this.client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse<String> getTotalConversion(Integer userId) {
        //Encode query param
        String query = String.format("%s",
                URLEncoder.encode(userId.toString(), StandardCharsets.UTF_8)
        );

        String fullUri = url + "/getTotalStorage/" + query;

        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(fullUri))
                .header("Accept", "application/json")
                .build();
        try {
            return this.client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch total conversion",e);
        }
    }

    @Override
    public HttpResponse<String> getStatistics(Integer userId,
                                              String startDate,
                                              String endDate,
                                              Integer fileTypeId) {
        //Encode query params
        String query = String.format("usuarioId=%s&fechaInicio=%s&fechaFin=%s&tipoArchivoId=%s",
                URLEncoder.encode(userId.toString(), StandardCharsets.UTF_8),
                URLEncoder.encode(startDate,   StandardCharsets.UTF_8),
                URLEncoder.encode(endDate,     StandardCharsets.UTF_8),
                URLEncoder.encode(fileTypeId.toString(), StandardCharsets.UTF_8)
        );

        String fullUri = url + "/getStatistics?" + query;

        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(fullUri))
                .header("Accept", "application/json")
                .build();

        try {
            return this.client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch statistics", e);
        }
    }
}
