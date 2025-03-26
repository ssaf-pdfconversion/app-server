package co.edu.upb.app.infrastructure;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.app.domain.models.Statistics;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class StorageServer implements InterfaceStorage {

    private final HttpClient client;
    private final String url;

    public StorageServer(){
        this.client = HttpClient.newHttpClient();
        this.url = Environment.getInstance().getDotenv().get("STORAGE_URL");
    }

    @Override
    public HttpResponse<Boolean> storeMetadata(Metadata data) {
        return null;
    }

    @Override
    public HttpResponse<Double> getTotalConversion(Integer userId) {
        return null;
    }

    @Override
    public HttpResponse<Statistics> getStatistics(Integer userId, String startDate, String endDate, Integer fileTypeId) {
        return null;
    }
}
