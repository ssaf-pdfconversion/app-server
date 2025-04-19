package co.edu.upb.app.domain.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OfficeFile")
@XmlAccessorType(XmlAccessType.FIELD)
public class OfficeFile {

    @XmlElement(name = "fileBase64", required = true)
    private String fileBase64;

    @XmlElement(name = "fileName", required = true)
    private String fileName;


    public OfficeFile() { }

    public OfficeFile(String fileBase64, String fileName) {
        this.fileBase64 = fileBase64;
        this.fileName = fileName;
    }

    public String getFileBase64(){
        return this.fileBase64;
    }

    public String getFileName(){
        return this.fileName;
    }

    public void setFileBase64(String fileBase64){
        this.fileBase64 = fileBase64;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
}