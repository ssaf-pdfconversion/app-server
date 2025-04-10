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
    public SOAPASResponse getOfficeConversion(String[] files, Integer userId) {

        AppResponse<String[]> appResponse = this.conversionManager.queueOfficeConversion(files);

        System.out.println("Ejecutando office conversion con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());

        return new SOAPASResponse(appResponse.isSuccess(), appResponse.getMessage(), files, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPASResponse getURLConversion(String[] urls, Integer userId) {
        AppResponse<String[]> appResponse = this.conversionManager.queueURLConversion(urls);

        System.out.println("Ejecutando url conversion con timestamp " + getNowTimestamp() + " con message " + appResponse.getMessage());

        return new SOAPASResponse(appResponse.isSuccess(), appResponse.getMessage(), urls, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId) {
        Double appResponse = this.metricsManager.getTotalConversion(userId);

        System.out.println("Ejecutando total conversion con timestamp " + getNowTimestamp() + " con message " + appResponse);

        return new SOAPDResponse(true, "Este es un mensaje de éxito para obtención del total de conversión", appResponse, getNowTimestamp());
    }

    @Override
    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter) {
        Statistics appResponse = this.metricsManager.getStatistics(userId, filter.getStartDate(), filter.getEndDate(), filter.getFileTypeId());

        System.out.println("Ejecutando statistics con timestamp " + getNowTimestamp() + " con message " + appResponse);

        return new SOAPStatsResponse(true, "Este es un mensaje de éxito para obtención de las estadísticas", appResponse, getNowTimestamp());
    }

    private String getNowTimestamp(){
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return now.format(formatter);
    }
}
