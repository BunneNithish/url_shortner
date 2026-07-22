package com.neueda.interview.urlshortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "visit")
@Table(name = "visit")
public class VisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_id", nullable = false)
    private Long urlId;

    @Column(name = "click_timestamp", nullable = false)
    private LocalDateTime clickTimestamp = LocalDateTime.now();

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "device", length = 100)
    private String device;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "referrer", length = 255)
    private String referrer;

    public VisitEntity() {
    }

    public VisitEntity(Long urlId, String browser, String operatingSystem, String device, String country, String referrer) {
        this.urlId = urlId;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.device = device;
        this.country = country;
        this.referrer = referrer;
        this.clickTimestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUrlId() {
        return urlId;
    }

    public void setUrlId(Long urlId) {
        this.urlId = urlId;
    }

    public LocalDateTime getClickTimestamp() {
        return clickTimestamp;
    }

    public void setClickTimestamp(LocalDateTime clickTimestamp) {
        this.clickTimestamp = clickTimestamp;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
