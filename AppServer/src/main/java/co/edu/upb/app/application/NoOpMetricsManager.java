package co.edu.upb.app.application;
import co.edu.upb.app.domain.models.Statistics;
import co.edu.upb.app.domain.models.storage.Transaction;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;

public class NoOpMetricsManager implements IMetricsManager {
    @Override
    public AppResponse<Boolean> storeMetadata(Transaction txn) {
        // Always succeed without doing anything
        return new AppResponse<>(true, "No-Op stub: metadata skipped", true);
    }

    @Override
    public AppResponse<Double> getTotalConversion(int userId) {
        return null;
    }

    @Override
    public AppResponse<Statistics[]> getStatistics(int userId, String startDate, String endDate, Integer fileTypeId) {
        return null;
    }
}

