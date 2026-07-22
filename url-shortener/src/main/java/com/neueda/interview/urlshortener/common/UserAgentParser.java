package com.neueda.interview.urlshortener.common;

public class UserAgentParser {

    public static class UserAgentDetails {
        private final String browser;
        private final String operatingSystem;
        private final String device;

        public UserAgentDetails(String browser, String operatingSystem, String device) {
            this.browser = browser;
            this.operatingSystem = operatingSystem;
            this.device = device;
        }

        public String getBrowser() {
            return browser;
        }

        public String getOperatingSystem() {
            return operatingSystem;
        }

        public String getDevice() {
            return device;
        }
    }

    public static UserAgentDetails parse(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return new UserAgentDetails("Unknown", "Unknown", "Desktop");
        }

        String os = "Unknown";
        String device = "Desktop";
        String browser = "Other";

        // Determine OS and Device
        if (userAgent.contains("Windows Phone")) {
            os = "Windows Phone";
            device = "Mobile";
        } else if (userAgent.contains("Android")) {
            os = "Android";
            if (userAgent.contains("Mobile")) {
                device = "Mobile";
            } else {
                device = "Tablet";
            }
        } else if (userAgent.contains("iPhone")) {
            os = "iOS";
            device = "Mobile";
        } else if (userAgent.contains("iPad")) {
            os = "iOS";
            device = "Tablet";
        } else if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS X")) {
            os = "macOS";
            device = "Desktop";
        } else if (userAgent.contains("Windows")) {
            os = "Windows";
            device = "Desktop";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
            device = "Desktop";
        }

        // Determine Browser
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) {
            browser = "Opera";
        } else if (userAgent.contains("Edg/") || userAgent.contains("Edge")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari")) {
            browser = "Safari";
        }

        return new UserAgentDetails(browser, os, device);
    }
}
