package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.app.domain.models.Statistics;

public interface IMetricsManager {
    public AppResponse<Boolean> storeMetadata(Transaction data);
    public AppResponse<Double> getTotalConversion(int userId);
    public AppResponse<Statistics[]> getStatistics(int userId, String startDate, String endDate, Integer fileTypeId);

}
