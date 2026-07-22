package com.neueda.interview.urlshortener.dto;

import java.util.List;

public class AnalyticsResponse {
    
    public static class DataPoint {
        private String name;
        private long value;

        public DataPoint() {
        }

        public DataPoint(String name, long value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    private long totalClicks;
    private List<DataPoint> dailyClicks;
    private List<DataPoint> topReferrers;
    private List<DataPoint> browserDistribution;
    private List<DataPoint> osDistribution;
    private List<DataPoint> deviceDistribution;
    private List<DataPoint> countryDistribution;

    public AnalyticsResponse() {
    }

    public AnalyticsResponse(long totalClicks, List<DataPoint> dailyClicks, List<DataPoint> topReferrers,
                             List<DataPoint> browserDistribution, List<DataPoint> osDistribution,
                             List<DataPoint> deviceDistribution, List<DataPoint> countryDistribution) {
        this.totalClicks = totalClicks;
        this.dailyClicks = dailyClicks;
        this.topReferrers = topReferrers;
        this.browserDistribution = browserDistribution;
        this.osDistribution = osDistribution;
        this.deviceDistribution = deviceDistribution;
        this.countryDistribution = countryDistribution;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public List<DataPoint> getDailyClicks() {
        return dailyClicks;
    }

    public void setDailyClicks(List<DataPoint> dailyClicks) {
        this.dailyClicks = dailyClicks;
    }

    public List<DataPoint> getTopReferrers() {
        return topReferrers;
    }

    public void setTopReferrers(List<DataPoint> topReferrers) {
        this.topReferrers = topReferrers;
    }

    public List<DataPoint> getBrowserDistribution() {
        return browserDistribution;
    }

    public void setBrowserDistribution(List<DataPoint> browserDistribution) {
        this.browserDistribution = browserDistribution;
    }

    public List<DataPoint> getOsDistribution() {
        return osDistribution;
    }

    public void setOsDistribution(List<DataPoint> osDistribution) {
        this.osDistribution = osDistribution;
    }

    public List<DataPoint> getDeviceDistribution() {
        return deviceDistribution;
    }

    public void setDeviceDistribution(List<DataPoint> deviceDistribution) {
        this.deviceDistribution = deviceDistribution;
    }

    public List<DataPoint> getCountryDistribution() {
        return countryDistribution;
    }

    public void setCountryDistribution(List<DataPoint> countryDistribution) {
        this.countryDistribution = countryDistribution;
    }
}
