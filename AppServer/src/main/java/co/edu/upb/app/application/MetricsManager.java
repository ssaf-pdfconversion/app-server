package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.data.Data;
import co.edu.upb.app.domain.models.data.DataBoolean;
import co.edu.upb.app.domain.models.data.DataDouble;
import co.edu.upb.app.domain.models.data.DataStatistics;
import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.app.domain.models.Statistics;
import com.google.gson.Gson;

import java.net.http.HttpResponse;

public class MetricsManager implements IMetricsManager {

    private final InterfaceStorage storage;

    public MetricsManager(InterfaceStorage storage){
        this.storage=storage;
    }

    @Override
    public AppResponse<Boolean> storeMetadata(Transaction data) {
        HttpResponse<String> resp = storage.storeMetadata(data);
        String json = resp.body();

        //parse into the DataBoolean class
        Gson gson = new Gson();
        DataBoolean dataBool = gson.fromJson(json, DataBoolean.class);
        return new AppResponse<>(dataBool.getStatus(), dataBool.getMessage(), dataBool.getData());
    }

    @Override
    public AppResponse<Double> getTotalConversion(int userId) {
        HttpResponse<String> resp = storage.getTotalConversion(userId);
        String json = resp.body();

        //parse into the DataDouble class
        Gson gson = new Gson();
        DataDouble data = gson.fromJson(json, DataDouble.class);
        return new AppResponse<>(data.getStatus(), data.getMessage(), data.getData());
    }

    @Override
    public AppResponse<Statistics[]> getStatistics(int userId, String startDate, String endDate, Integer fileTypeId) {
        HttpResponse<String> resp = storage.getStatistics(userId, startDate, endDate, fileTypeId);
        String json = resp.body();

        //parse into the DataStatistics class
        Gson gson = new Gson();
        DataStatistics data = gson.fromJson(json, DataStatistics.class);
        return new AppResponse<>(data.getStatus(), data.getMessage(), data.getData());
    }
}
