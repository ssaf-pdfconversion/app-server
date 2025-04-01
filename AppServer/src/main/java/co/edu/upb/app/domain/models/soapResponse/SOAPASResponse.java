package co.edu.upb.app.domain.models.soapResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SOAPASResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPASResponse extends SOAPResponse<String[]> { //This is a class for SOAP responses corresponding String Array related content.
    public SOAPASResponse(){super();}

    public SOAPASResponse(boolean success, String message, String[] content, String timestamp){
        super(success, message, content, timestamp);
    }
}
