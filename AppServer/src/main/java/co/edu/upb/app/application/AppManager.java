package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.OfficeFile;
import co.edu.upb.app.domain.models.Statistics;
import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.*;
import co.edu.upb.node.domain.models.ConvertedFile;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

import java.time.Instant;

@WebService(
        endpointInterface = "co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp",
        serviceName = "AppService",
        portName = "AppPort",
        targetNamespace = "http://interfaces.domain.app.upb.edu.co/"
)
public class AppManager implements InterfaceApp {

    private final IAuthManager authManager;
    private final IConversionManager conversionManager;
    private final IMetricsManager metricsManager;

    public AppManager(IAuthManager authManager, IConversionManager conversionManager, IMetricsManager metricsManager){
        this.authManager = authManager;
        this.conversionManager = conversionManager;
        this.metricsManager = metricsManager;
    }

    @Override
    @WebMethod
    public SOAPSResponse login(String username, String password) {

        AppResponse<String> appResponse = this.authManager.login(username, password);

        System.out.println("Ejecutando login con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());
        return new SOAPSResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPSResponse register(String username, String password, String nombre, String apellido, String email) {
        AppResponse<String> appResponse = this.authManager.register(username, password, nombre, apellido, email);

        System.out.println("Ejecutando register con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());
        return new SOAPSResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPBResponse validate(String jwt) {
        AppResponse<Boolean> appResponse = this.authManager.validateJWT(jwt);

        System.out.println("Ejecutando validate con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());
        return new SOAPBResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPConvResponse getOfficeConversion(OfficeFile[] files, Integer userId) {
        AppResponse<ConvertedFile[]> appResponse = this.conversionManager.queueOfficeConversion(files, userId);

        System.out.println("Ejecutando office conversion con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());
        return new SOAPConvResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPConvResponse getURLConversion(String[] urls, Integer userId) {
        AppResponse<ConvertedFile[]> appResponse = this.conversionManager.queueURLConversion(urls, userId);

        System.out.println("Ejecutando url conversion con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());
        return new SOAPConvResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId) {
        AppResponse<Double> appResponse = this.metricsManager.getTotalConversion(userId);

        double rounded = Math.round(appResponse.getData() * 100.0) / 100.0;
        appResponse.setData(rounded);

        System.out.println("Ejecutando total conversion con timestamp " + getNowTimestamp() + " con message " + appResponse.getData());

        return new SOAPDResponse(true, "Este es un mensaje de éxito para obtención del total de conversión", appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter) {
        AppResponse<Statistics[]> appResponse = this.metricsManager.getStatistics(userId, filter.getStartDate(), filter.getEndDate(), filter.getFileTypeId());

        System.out.println("Ejecutando statistics con timestamp " + getNowTimestamp() + " con message " + appResponse);

        return new SOAPStatsResponse(true, "Este es un mensaje de éxito para obtención de las estadísticas", appResponse.getData(), getNowTimestamp());
    }

    private String getNowTimestamp(){
        return Instant.now().toString();
    }
}
