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
import java.time.Instant;

public class MetricsManager implements IMetricsManager {

    private final InterfaceStorage storage;

    public MetricsManager(InterfaceStorage storage){
        this.storage=storage;
    }

    @Override
    public AppResponse<Boolean> storeMetadata(Transaction data) {
        try {
            HttpResponse<String> resp = storage.storeMetadata(data);
            String json = resp.body();

            //parse into the DataBoolean class
            Gson gson = new Gson();
            DataBoolean dataBool = gson.fromJson(json, DataBoolean.class);
            return new AppResponse<>(dataBool.getStatus(), dataBool.getMessage(), dataBool.getData());
        } catch (Exception e) {
            return new AppResponse<>(false, "Data couldn't be stored", false);
        }
    }

    @Override
    public AppResponse<Double> getTotalConversion(int userId) {
        try {
            HttpResponse<String> resp = storage.getTotalConversion(userId);
            String json = resp.body();

            //parse into the DataDouble class
            Gson gson = new Gson();
            System.out.println(json);
            DataDouble data = gson.fromJson(json, DataDouble.class);

            System.out.println(data.getData());

            return new AppResponse<>(data.getStatus(), data.getMessage(), data.getData());
        } catch (Exception e) {
            e.printStackTrace();
            return new AppResponse<>(false, "Total conversion couldn't be fetched", 0.0);
        }
    }

    @Override
    public AppResponse<Statistics[]> getStatistics(int userId, String startDate, String endDate, Integer fileTypeId) {

        try {
            HttpResponse<String> resp = storage.getStatistics(userId, startDate, endDate, fileTypeId);
            String json = resp.body();

            System.out.println(json);

            //parse into the DataStatistics class
            Gson gson = new Gson();
            DataStatistics data = gson.fromJson(json, DataStatistics.class);
            Statistics[] statsData = data.getData();

            if (data.getData().length == 0){

                Instant timestamp = Instant.now();
                statsData = new Statistics[0];

                System.out.println("NO DATA RETRIEVED " + timestamp.toString());
            }

            return new AppResponse<>(data.getStatus(), data.getMessage(), statsData);
        } catch (Exception e) {
            return new AppResponse<>(false, "Data couldn't be stored", new Statistics[0]);
        }
    }
}
