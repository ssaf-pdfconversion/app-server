package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.Metadata;
import co.edu.upb.app.domain.models.Statistics;

import java.net.http.HttpResponse;

public interface InterfaceStorage {
    public HttpResponse<Boolean> storeMetadata(Metadata data);
    public HttpResponse<Double> getTotalConversion(Integer userId);
    public HttpResponse<Statistics> getStatistics(Integer userId, String startDate, String endDate, Integer fileTypeId);
}
