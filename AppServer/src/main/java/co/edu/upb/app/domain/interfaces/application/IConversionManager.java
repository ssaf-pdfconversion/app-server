package co.edu.upb.app.domain.interfaces.application;

import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.OfficeFile;
import co.edu.upb.node.domain.models.ConvertedFile;

public interface IConversionManager {
    public AppResponse<ConvertedFile[]> queueOfficeConversion(OfficeFile[] files, Integer userId);
    public AppResponse<ConvertedFile[]> queueURLConversion(String[] files, Integer userId);
}
