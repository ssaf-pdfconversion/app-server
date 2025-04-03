package co.edu.upb.app.domain.models.soapResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SOAPBResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPBResponse extends SOAPResponse<Boolean> { //This is a class for SOAP responses corresponding Boolean related content.
    public SOAPBResponse(){super();}

    public SOAPBResponse(boolean success, String message, Boolean content, String timestamp){
        super(success, message, content, timestamp);
    }

    @XmlElement(name = "content")
    @Override
    public Boolean getContent() {
        return super.getContent();
    }
}