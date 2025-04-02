package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.Metadata;

import java.net.http.HttpResponse;

public interface InterfaceStorage {
    public HttpResponse<String> storeMetadata(Metadata data);
    public HttpResponse<String> getTotalConversion(Integer userId);
    public HttpResponse<String> getStatistics(Integer userId, String startDate, String endDate, Integer fileTypeId);
}
