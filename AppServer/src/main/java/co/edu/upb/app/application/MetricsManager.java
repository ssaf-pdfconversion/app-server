package co.edu.upb.app.application;

import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceStorage;
import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.app.domain.models.Statistics;

public class MetricsManager implements IMetricsManager {

    private InterfaceStorage storage;

    public MetricsManager(InterfaceStorage storage){
        this.storage=storage;
    }

    @Override
    public Boolean storeMetadata(Metadata data) {
        return null;
    }

    @Override
    public Double getTotalConversion(int userId) {
        return 0.0;
    }

    @Override
    public Statistics getStatistics(int userId, String startDate, String endDate, Integer fileTypeId) {
        return null;
    }
}
