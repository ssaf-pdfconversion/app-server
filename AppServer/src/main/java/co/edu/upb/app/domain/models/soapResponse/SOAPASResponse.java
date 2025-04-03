package co.edu.upb.app.domain.models.soapResponse;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

@XmlRootElement(name = "SOAPASResponse")
@XmlAccessorType(XmlAccessType.NONE) // Use NONE so that only annotated methods/properties are used.
public class SOAPASResponse extends SOAPResponse<String[]> {

    public SOAPASResponse() {
        super();
    }

    public SOAPASResponse(boolean success, String message, String[] content, String timestamp){
        super(success, message, content, timestamp);
    }

    // Here we override getContent() to wrap the array
    @XmlElementWrapper(name = "files")  // This creates a parent <files> element
    @XmlElement(name = "file")             // Each element in the array will appear as <file>
    @Override
    public String[] getContent() {
        return super.getContent();
    }
}
