package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.app.domain.models.Statistics;

public class MetricsManager implements IMetricsManager {

    private final InterfaceStorage storage;

    public MetricsManager(InterfaceStorage storage){
        this.storage=storage;
    }

    @Override
    public Boolean storeMetadata(Transaction data) {
        return Boolean.parseBoolean(storage.storeMetadata(data).body());
    }

    @Override
    public Double getTotalConversion(int userId) {
        return Double.parseDouble(storage.getTotalConversion(userId).body());
    }

    @Override
    public Statistics getStatistics(int userId, String startDate, String endDate, Integer fileTypeId) {
        return new Statistics();
    }
}
