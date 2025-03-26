package co.edu.upb.app.domain.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "StatsFilter")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatsFilter {

    @XmlElement(name = "startDate", required = true)
    private String startDate;

    @XmlElement(name = "endDate", required = true)
    private String endDate;

    @XmlElement(name = "fileTypeId", required = false, nillable = true)
    private Integer fileTypeId;

    public StatsFilter() { }

    public StatsFilter(String startDate, String endDate, Integer fileTypeId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileTypeId = fileTypeId;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public Integer getFileTypeId() {
        return fileTypeId;
    }
    public void setFileTypeId(Integer fileTypeId) {
        this.fileTypeId = fileTypeId;
    }
}