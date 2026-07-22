package com.neueda.interview.urlshortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "url")
@Table(name = "url")
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "full_url", nullable = false, length = 2048)
    private String fullUrl;

    @Column(name = "short_url", nullable = false, unique = true)
    private String shortUrl = java.util.UUID.randomUUID().toString();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public UrlEntity() {
    }

    public UrlEntity(Long id, String title, String fullUrl, String shortUrl, LocalDateTime expiryDate, boolean isEnabled) {
        this.id = id;
        this.title = title;
        this.fullUrl = fullUrl;
        this.shortUrl = shortUrl;
        this.expiryDate = expiryDate;
        this.isEnabled = isEnabled;
        this.createdAt = LocalDateTime.now();
    }

    public UrlEntity(String fullUrl) {
        this.fullUrl = fullUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return "UrlEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", fullUrl='" + fullUrl + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", createdAt=" + createdAt +
                ", expiryDate=" + expiryDate +
                ", isEnabled=" + isEnabled +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
