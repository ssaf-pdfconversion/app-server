package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.storage.Metadata;
import co.edu.upb.app.domain.models.storage.Transaction;
import com.google.gson.Gson;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class StorageClient implements InterfaceStorage {

    private final HttpClient client;
    private final String url;

    public StorageClient() throws Exception {
        this.url = Environment.getInstance().getDotenv().get("STORAGE_URL");

        // --- BUILD AN “INSECURE” SSL CONTEXT ---
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new SecureRandom());

        // --- DISABLE HOSTNAME VERIFICATION ---
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("");

        // --- BUILD YOUR HTTP CLIENT ---
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(sslContext)
                .sslParameters(sslParams)
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
