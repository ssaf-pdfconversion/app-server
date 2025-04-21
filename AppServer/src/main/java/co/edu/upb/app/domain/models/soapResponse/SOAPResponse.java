package co.edu.upb.app.domain.models.soapResponse;

import co.edu.upb.app.domain.models.Statistics;
import co.edu.upb.node.domain.models.ConvertedFile;
import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "SOAPResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ Statistics.class, ConvertedFile.class})
public class SOAPResponse<DataType> { //Generic class to wrap content with useful information.
    @XmlElement(name = "success", required = true)
    private boolean success;

    @XmlElement(name = "message", required = true)
    private String message;

    @XmlElement(name = "content", required = true)
    private DataType content;

    @XmlElement(name = "timestamp", required = true)
    private String timestamp;

    public SOAPResponse(){
    }

    public SOAPResponse(boolean success, String message, DataType content, String timestamp){
        this.success = success;
        this.message = message;
        this.content = content;
        this.timestamp = timestamp;
    }

    public boolean getSuccess(){
        return this.success;

    }

    public String getMessage(){
        return this.message;

    }

    public DataType getContent(){
        return this.content;

    }

    public String getTimestamp(){
        return this.timestamp;

    }
}
