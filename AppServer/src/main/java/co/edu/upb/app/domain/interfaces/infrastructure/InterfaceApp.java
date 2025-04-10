package co.edu.upb.app.domain.interfaces.infrastructure;

import co.edu.upb.app.domain.models.StatsFilter;
import co.edu.upb.app.domain.models.soapResponse.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;

@WebService(targetNamespace = "http://interfaces.domain.app.upb.edu.co/", name = "InterfaceApp")
public interface InterfaceApp {

    @WebMethod
    public SOAPSResponse login(
            @WebParam(name = "username", partName = "username")
            @XmlElement(name = "username", required = true, nillable = false)
            String username,
            @WebParam(name = "password", partName = "password")
            @XmlElement(name = "password", required = true, nillable = false)
            String password);

    @WebMethod
    public SOAPSResponse register(
            @WebParam(name = "username", partName = "username")
            @XmlElement(name = "username", required = true, nillable = false)
            String username,
            @WebParam(name = "password", partName = "password")
            @XmlElement(name = "password", required = true, nillable = false)
            String password,
            @WebParam(name = "nombre", partName = "nombre")
            @XmlElement(name = "nombre", required = true, nillable = false)
            String nombre,
            @WebParam(name = "apellido", partName = "apellido")
            @XmlElement(name = "apellido", required = true, nillable = false)
            String apellido,
            @WebParam(name = "email", partName = "email")
            @XmlElement(name = "email", required = true, nillable = false)
            String email);

    @WebMethod
    public SOAPBResponse validate(
            @WebParam(name = "jwt", partName = "jwt")
            @XmlElement(name = "jwt", required = true, nillable = false)
            String jwt);

    @WebMethod
    public SOAPASResponse getOfficeConversion(
            @WebParam(name = "files", partName = "files")
            @XmlElement(name = "files", required = true, nillable = false)
            String[] files,
            @WebParam(name = "userId", partName = "userId")
            @XmlElement(name = "userId", required = true, nillable = false)
            Integer userId
            );

    @WebMethod
    public SOAPASResponse getURLConversion(
            @WebParam(name = "urls", partName = "urls")
            @XmlElement(name = "urls", required = true, nillable = false)
            String[] urls,
            @WebParam(name = "userId", partName = "userId")
            @XmlElement(name = "userId", required = true, nillable = false)
            Integer userId);

    @WebMethod
    public SOAPDResponse getTotalConversion(
            @WebParam(name = "userId", partName = "userId")
            @XmlElement(name = "userId", required = true, nillable = false)
            Integer userId);

    @WebMethod
    public SOAPStatsResponse getStatistics(
            @WebParam(name = "userId", partName = "userId")
            @XmlElement(name = "userId", required = true, nillable = false)
            Integer userId,
            @WebParam(name = "filter", partName = "filter")
            @XmlElement(name = "filter", required = true, nillable = false)
            StatsFilter filter);
}
