package co.edu.upb.app.domain.models.soapResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SOAPDResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPDResponse extends SOAPResponse<Double> { //This is a class for SOAP responses corresponding Double related content.
    public SOAPDResponse(){super();}

    public SOAPDResponse(boolean success, String message, Double content, String timestamp){
        super(success, message, content, timestamp);
    }
}