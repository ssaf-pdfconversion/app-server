package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.app.domain.models.Statistics;

import java.util.Optional;

public interface IMetricsManager {
    public Boolean storeMetadata(Metadata data);
    public Double getTotalConversion(int userId);
    public Statistics getStatistics(int userId, String startDate, String endDate, Integer fileTypeId);

}
