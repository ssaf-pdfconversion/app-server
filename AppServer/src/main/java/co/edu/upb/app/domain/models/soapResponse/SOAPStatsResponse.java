package co.edu.upb.app.domain.models.soapResponse;

import co.edu.upb.app.domain.models.Statistics;
import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "SOAPStatsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPStatsResponse extends SOAPResponse<Statistics[]> { //This is a class for SOAP responses corresponding Statistics Array related content.
    public SOAPStatsResponse(){super();}

    public SOAPStatsResponse(boolean success, String message, Statistics[] content, String timestamp){
        super(success, message, content, timestamp);
    }

    @XmlElementWrapper(name = "stats")  // This creates a parent <stats> element
    @XmlElement(name = "stat")
    @Override
    public Statistics[] getContent() {
        return super.getContent();
    }
}