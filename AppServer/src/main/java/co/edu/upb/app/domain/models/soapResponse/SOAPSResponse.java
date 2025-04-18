package co.edu.upb.app.domain.models.soapResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SOAPSResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPSResponse extends SOAPResponse<String> { //This is a class for SOAP responses corresponding String related content.
    public SOAPSResponse(){super();}

    public SOAPSResponse(boolean success, String message, String content, String timestamp){
        super(success, message, content, timestamp);
    }

    @XmlElement(name = "content")
    @Override
    public String getContent() {
        return super.getContent();
    }
}
