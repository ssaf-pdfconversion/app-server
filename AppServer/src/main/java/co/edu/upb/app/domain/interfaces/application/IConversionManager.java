package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.AppResponse;

public interface IConversionManager {
    public AppResponse<String[]> queueOfficeConversion(String[] files);
    public AppResponse<String[]> queueURLConversion(String[] files);
}
