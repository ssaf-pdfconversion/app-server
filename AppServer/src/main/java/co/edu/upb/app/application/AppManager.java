package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import co.edu.upb.app.domain.models.AppResponse;
import co.edu.upb.app.domain.models.Statistics;
import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
        System.out.println("Ejecutando login con timestamp " + getNowTimestamp());

        AppResponse<String> appResponse = this.authManager.login(username, password);
        return new SOAPSResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPSResponse register(String username, String password, String nombre, String apellido, String email) {
        System.out.println("Ejecutando register con timestamp " + getNowTimestamp());

        AppResponse<String> appResponse = this.authManager.register(username, password, nombre, apellido, email);
        return new SOAPSResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPBResponse validate(String jwt) {
        System.out.println("Ejecutando validate con timestamp " + getNowTimestamp());

        AppResponse<Boolean> appResponse = this.authManager.validateJWT(jwt);
        return new SOAPBResponse(appResponse.isSuccess(), appResponse.getMessage(), appResponse.getData(), getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPASResponse getOfficeConversion(String[] files) {

        System.out.println("Ejecutando office conversion con timestamp " + getNowTimestamp());

        AppResponse<String[]> appResponse = this.conversionManager.queueOfficeConversion(files);
        return new SOAPASResponse(appResponse.isSuccess(), appResponse.getMessage(), files, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPASResponse getURLConversion(String[] urls) {
        System.out.println("Ejecutando URL conversion con timestamp " + getNowTimestamp());

        AppResponse<String[]> appResponse = this.conversionManager.queueURLConversion(urls);
        return new SOAPASResponse(appResponse.isSuccess(), appResponse.getMessage(), urls, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId) {
        System.out.println("Ejecutando getTotalConversion con timestamp " + getNowTimestamp());

        Double appResponse = this.metricsManager.getTotalConversion(userId);
        return new SOAPDResponse(true, "Este es un mensaje de éxito para obtención del total de conversión", appResponse, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter) {
        System.out.println("Ejecutando getStatistics con timestamp " + getNowTimestamp());

        Statistics appResponse = this.metricsManager.getStatistics(userId, filter.getStartDate(), filter.getEndDate(), filter.getFileTypeId());
        return new SOAPStatsResponse(true, "Este es un mensaje de éxito para obtención de las estadísticas", appResponse, getNowTimestamp());
    }

    private String getNowTimestamp(){
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return now.format(formatter);
    }
}
