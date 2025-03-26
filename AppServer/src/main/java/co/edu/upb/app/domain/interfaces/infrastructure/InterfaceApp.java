package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.SOAPBResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPDResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPSResponse;
import co.edu.upb.app.domain.models.soapResponse.SOAPStatsResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(targetNamespace = "http://interfaces.domain.app.upb.edu.co/", name = "InterfaceApp")
public interface InterfaceApp {
    @WebMethod
    public SOAPSResponse login(String username, String password);

    @WebMethod
    public SOAPSResponse register(String username, String password, String nombre, String apellido, String email);

    @WebMethod
    public SOAPBResponse validate(String jwt);

    @WebMethod
    public SOAPSResponse getOfficeConversion(String[] files);

    @WebMethod
    public SOAPSResponse getURLConversion(String[] urls);

    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId);

    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter);
}
