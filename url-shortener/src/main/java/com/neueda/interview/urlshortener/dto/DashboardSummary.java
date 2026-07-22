package com.neueda.interview.urlshortener.dto;

public class DashboardSummary {
    private long totalLinks;
    private long totalClicks;
    private long activeLinks;
    private long expiredLinks;

    public DashboardSummary() {
    }

    public DashboardSummary(long totalLinks, long totalClicks, long activeLinks, long expiredLinks) {
        this.totalLinks = totalLinks;
        this.totalClicks = totalClicks;
        this.activeLinks = activeLinks;
        this.expiredLinks = expiredLinks;
    }

    public long getTotalLinks() {
        return totalLinks;
    }

    public void setTotalLinks(long totalLinks) {
        this.totalLinks = totalLinks;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public long getActiveLinks() {
        return activeLinks;
    }

    public void setActiveLinks(long activeLinks) {
        this.activeLinks = activeLinks;
    }

    public long getExpiredLinks() {
        return expiredLinks;
    }

    public void setExpiredLinks(long expiredLinks) {
        this.expiredLinks = expiredLinks;
    }
}
