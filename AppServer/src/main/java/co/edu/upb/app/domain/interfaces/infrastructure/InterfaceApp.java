package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.*;
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
    public SOAPASResponse getOfficeConversion(String[] files);

    @WebMethod
    public SOAPASResponse getURLConversion(String[] urls);

    @WebMethod
    public SOAPDResponse getTotalConversion(Integer userId);

    @WebMethod
    public SOAPStatsResponse getStatistics(Integer userId, StatsFilter filter);
}
