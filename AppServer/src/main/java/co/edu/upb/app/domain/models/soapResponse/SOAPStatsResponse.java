package co.edu.upb.app.domain.models.soapResponse;

import co.edu.upb.app.domain.models.Statistics;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SOAPStatsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPStatsResponse extends SOAPResponse<Statistics> { //This is a class for SOAP responses corresponding Statistics related content.
    public SOAPStatsResponse(){super();}

    public SOAPStatsResponse(boolean success, String message, Statistics content, String timestamp){
        super(success, message, content, timestamp);
    }

    @XmlElement(name = "content")
    @Override
    public Statistics getContent() {
        return super.getContent();
    }
}