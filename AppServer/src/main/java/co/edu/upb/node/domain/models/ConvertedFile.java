package co.edu.upb.node.domain.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConvertedFile")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConvertedFile {

    @XmlElement(name = "data", required = true)
    private String data;

    @XmlElement(name = "filename", required = true)
    private String filename;

    @XmlElement(name = "conversionSuccess", required = true)
    private Boolean conversionSuccess;

    public ConvertedFile() {
    }

    public ConvertedFile(String data, String filename, Boolean conversionSuccess) {
        this.data = data;
        this.filename = filename;
        this.conversionSuccess = conversionSuccess;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Boolean getConversionSuccess() {
        return conversionSuccess;
    }
    public void setConversionSuccess(Boolean conversionSuccess) {
        this.conversionSuccess = conversionSuccess;
    }
}
