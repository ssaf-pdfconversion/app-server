package co.edu.upb.app.domain.models.soapResponse;

import co.edu.upb.node.domain.models.ConvertedFile;
import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "SOAPConvResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPConvResponse extends SOAPResponse<ConvertedFile[]> { //This is a class for SOAP responses corresponding ConvertedFile related content.
    public SOAPConvResponse(){super();}

    public SOAPConvResponse(boolean success, String message, ConvertedFile[] content, String timestamp){
        super(success, message, content, timestamp);
    }

    @XmlElementWrapper(name = "files")  // This creates a parent <files> element
    @XmlElement(name = "file")
    @Override
    public ConvertedFile[] getContent() {
        return super.getContent();
    }
}