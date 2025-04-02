package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.Metadata;

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
    public HttpResponse<String> storeMetadata(Metadata data) {

        String jsonRequestBody = String.format("""
            {
                "usuarioId": %d,
                "tipoArchivoId": %d,
                "size": %.2f%%,
                "fechaHora": %s
            }
            """, data.userId(), data.fileTypeId(), data.size(), data.timestamp());

        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .uri(URI.create(url + "/storeMetadata"))
                .header("Accept", "application/json")
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
