package co.edu.upb.app.application;

import co.edu.upb.app.domain.interfaces.application.IAuthManager;
import co.edu.upb.app.domain.interfaces.application.IConversionManager;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp;
import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.SOAPBResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPDResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPSResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPStatsResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(
        endpointInterface = "co.edu.upb.app.domain.interfaces.infrastructure.InterfaceApp",
        serviceName = "AppService",
        portName = "AppPort",
        targetNamespace = "http://interfaces.domain.app.upb.edu.co/"
)
public class AppManager implements InterfaceApp {

    public AppManager(IAuthManager authManager, IConversionManager conversionManager, IMetricsManager metricsManager){}

    @Override
    @WebMethod
    public SOAPSResponse login(String username, String password) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPSResponse register(String username, String password, String nombre, String apellido, String email) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPBResponse validate(String jwt) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPSResponse getOfficeConversion(String[] files) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPSResponse getURLConversion(String[] urls) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId) {
        return null;
    }

    @Override
    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter) {
        return null;
    }
}
