package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.storage.Metadata;
import co.edu.upb.app.domain.models.storage.Transaction;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StorageClient implements InterfaceStorage {

    private final HttpClient client;
    private final String url;

    public StorageClient(){
        this.url = Environment.getInstance().getDotenv().get("STORAGE_URL");
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
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
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/getTotalStorage"))
                .header("Accept", "application/json")
                .build();

        try {
            return this.client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse<String> getStatistics(Integer userId, String startDate, String endDate, Integer fileTypeId) {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/getStatistics"))
                .header("Accept", "application/json")
                .build();

        try {
            return this.client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
