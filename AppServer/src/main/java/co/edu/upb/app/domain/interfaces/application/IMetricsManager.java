package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.app.domain.models.Statistics;

public interface IMetricsManager {
    public Boolean storeMetadata(Transaction data);
    public Double getTotalConversion(int userId);
    public Statistics getStatistics(int userId, String startDate, String endDate, Integer fileTypeId);

}
