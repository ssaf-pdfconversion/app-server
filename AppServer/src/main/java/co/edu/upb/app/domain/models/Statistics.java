package co.edu.upb.app.domain.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Statistics")
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {

    @XmlElement(name = "totalMB", required = true)
    private Long totalMB;

    @XmlElement(name = "date", required = true)
    private String date;

    public Statistics() { }

    public Statistics(Long totalMB, String date) {
        this.totalMB = totalMB;
        this.date = date;
    }

    public Long getTotalMB() {
        return totalMB;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTotalMB(Long totalMB) {
        this.totalMB = totalMB;
    }
}