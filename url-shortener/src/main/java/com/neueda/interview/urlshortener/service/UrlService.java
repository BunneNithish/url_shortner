package com.neueda.interview.urlshortener.service;

import com.neueda.interview.urlshortener.common.GeoIPResolver;
import com.neueda.interview.urlshortener.common.ShorteningUtil;
import com.neueda.interview.urlshortener.common.UserAgentParser;
import com.neueda.interview.urlshortener.dto.*;
import com.neueda.interview.urlshortener.model.UrlEntity;
import com.neueda.interview.urlshortener.model.VisitEntity;
import com.neueda.interview.urlshortener.repository.UrlRepository;
import com.neueda.interview.urlshortener.repository.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final VisitRepository visitRepository;

    @Autowired
    public UrlService(UrlRepository urlRepository, VisitRepository visitRepository) {
        this.urlRepository = urlRepository;
        this.visitRepository = visitRepository;
    }

    /**
     * Retrieve the active full URL by its short URL / code, validate expiry and enabled status.
     */
    public UrlEntity getActiveUrlEntity(String shortenString) {
        UrlEntity entity = urlRepository.findByShortUrlAndIsDeletedFalse(shortenString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found"));

        if (!entity.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This short URL has been disabled");
        }

        if (entity.getExpiryDate() != null && entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This short URL has expired");
        }

        return entity;
    }

    /**
     * Legacy method fallback for redirection (or backward compatibility)
     */
    public FullUrl getFullUrl(String shortenString) {
        UrlEntity entity = getActiveUrlEntity(shortenString);
        return new FullUrl(entity.getFullUrl());
    }

    /**
     * Create a new shortened link with optional configurations.
     */
    public LinkResponse createLink(CreateLinkRequest request, String baseUrl) {
        if (request.getFullUrl() == null || request.getFullUrl().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full URL is required");
        }

        String fullUrl = request.getFullUrl().trim();
        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        String customAlias = request.getCustomAlias() != null ? request.getCustomAlias().trim() : "";
        LocalDateTime expiryDate = request.getExpiryDate();

        // If it's a basic auto-shorten request, reuse the existing matching auto-generated link
        if (customAlias.isEmpty() && (title.isEmpty() || "Untitled Link".equals(title)) && expiryDate == null) {
            List<UrlEntity> existing = urlRepository.findByFullUrl(fullUrl);
            Optional<UrlEntity> matched = existing.stream()
                    .filter(u -> !u.isDeleted() && u.isEnabled())
                    .filter(u -> u.getExpiryDate() == null)
                    .filter(u -> u.getShortUrl().equals(ShorteningUtil.idToStr(u.getId())))
                    .findFirst();
            if (matched.isPresent()) {
                return convertToResponse(matched.get(), baseUrl);
            }
        }

        UrlEntity entity = new UrlEntity();
        entity.setFullUrl(fullUrl);
        entity.setTitle(title.isEmpty() ? "Untitled Link" : title);
        entity.setExpiryDate(expiryDate);
        entity.setEnabled(true);
        entity.setDeleted(false);
        
        // Save initial entity to assign an auto-increment ID
        entity.setShortUrl(UUID.randomUUID().toString().substring(0, 8)); // temporary unique value
        entity = urlRepository.save(entity);

        if (!customAlias.isEmpty()) {
            if (!customAlias.matches("^[a-zA-Z0-9_-]+$")) {
                urlRepository.delete(entity);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom alias can only contain alphanumeric characters, hyphens, and underscores");
            }
            Optional<UrlEntity> existing = urlRepository.findByShortUrlAndIsDeletedFalse(customAlias);
            if (existing.isPresent()) {
                urlRepository.delete(entity);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Custom alias '" + customAlias + "' is already in use");
            }
            entity.setShortUrl(customAlias);
        } else {
            entity.setShortUrl(ShorteningUtil.idToStr(entity.getId()));
        }

        entity = urlRepository.save(entity);
        return convertToResponse(entity, baseUrl);
    }

    /**
     * Search and paginate all active short links.
     */
    public Page<LinkResponse> searchLinks(String search, Pageable pageable, String baseUrl) {
        Page<UrlEntity> entities;
        if (search == null || search.trim().isEmpty()) {
            entities = urlRepository.findAllActive(pageable);
        } else {
            entities = urlRepository.searchLinks(search.trim(), pageable);
        }

        return entities.map(entity -> convertToResponse(entity, baseUrl));
    }

    /**
     * Fetch a link details by its ID.
     */
    public LinkResponse getLinkDetails(Long id, String baseUrl) {
        UrlEntity entity = urlRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found"));
        return convertToResponse(entity, baseUrl);
    }

    /**
     * Update link details (title, alias, status, expiry).
     */
    public LinkResponse updateLink(Long id, UpdateLinkRequest request, String baseUrl) {
        UrlEntity entity = urlRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found"));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            entity.setTitle(title.isEmpty() ? "Untitled Link" : title);
        }

        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }

        // Handle updated custom alias/short code
        if (request.getCustomAlias() != null) {
            String newAlias = request.getCustomAlias().trim();
            if (newAlias.isEmpty()) {
                // If reset to empty, fall back to auto-generated ID code
                entity.setShortUrl(ShorteningUtil.idToStr(entity.getId()));
            } else if (!newAlias.equals(entity.getShortUrl())) {
                if (!newAlias.matches("^[a-zA-Z0-9_-]+$")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom alias can only contain alphanumeric characters, hyphens, and underscores");
                }
                Optional<UrlEntity> existing = urlRepository.findByShortUrlAndIsDeletedFalse(newAlias);
                if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Custom alias '" + newAlias + "' is already in use");
                }
                entity.setShortUrl(newAlias);
            }
        }

        // Allow clearing or setting new expiry date
        entity.setExpiryDate(request.getExpiryDate());

        entity = urlRepository.save(entity);
        return convertToResponse(entity, baseUrl);
    }

    /**
     * Soft delete a link.
     */
    public void deleteLink(Long id) {
        UrlEntity entity = urlRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found"));
        entity.setDeleted(true);
        urlRepository.save(entity);
    }

    /**
     * Register a new visit for analytics.
     */
    public void recordVisit(Long urlId, String ipAddress, String userAgent, String referrer) {
        UserAgentParser.UserAgentDetails details = UserAgentParser.parse(userAgent);
        String country = GeoIPResolver.resolveCountry(ipAddress);
        
        String cleanReferrer = "Direct";
        if (referrer != null && !referrer.trim().isEmpty() && !referrer.equals("null")) {
            cleanReferrer = referrer.trim();
            // Get root domain of referrer for cleaner tables
            try {
                java.net.URI uri = new java.net.URI(cleanReferrer);
                String host = uri.getHost();
                if (host != null) {
                    cleanReferrer = host.startsWith("www.") ? host.substring(4) : host;
                }
            } catch (Exception e) {
                // Keep raw referrer if parsing fails
            }
        }

        VisitEntity visit = new VisitEntity(urlId, details.getBrowser(), details.getOperatingSystem(), details.getDevice(), country, cleanReferrer);
        visitRepository.save(visit);
    }

    /**
     * Fetch complete charts data and summary metrics for a link's analytics dashboard.
     */
    public AnalyticsResponse getLinkAnalytics(Long id) {
        urlRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found"));

        List<VisitEntity> visits = visitRepository.findByUrlId(id);

        long totalClicks = visits.size();

        // 1. Group daily clicks (sorted chronologically)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, Long> dailyMap = visits.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getClickTimestamp().format(dateFormatter),
                        Collectors.counting()
                ));
        List<AnalyticsResponse.DataPoint> dailyClicks = dailyMap.entrySet().stream()
                .map(e -> new AnalyticsResponse.DataPoint(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(AnalyticsResponse.DataPoint::getName))
                .collect(Collectors.toList());

        // 2. Group by Browser
        List<AnalyticsResponse.DataPoint> browsers = getDistribution(visits.stream().map(VisitEntity::getBrowser));
        
        // 3. Group by OS
        List<AnalyticsResponse.DataPoint> os = getDistribution(visits.stream().map(VisitEntity::getOperatingSystem));
        
        // 4. Group by Device
        List<AnalyticsResponse.DataPoint> devices = getDistribution(visits.stream().map(VisitEntity::getDevice));
        
        // 5. Group by Country
        List<AnalyticsResponse.DataPoint> countries = getDistribution(visits.stream().map(VisitEntity::getCountry));
        
        // 6. Group by Referrer (Top Referrers)
        List<AnalyticsResponse.DataPoint> referrers = getDistribution(visits.stream().map(VisitEntity::getReferrer));

        return new AnalyticsResponse(totalClicks, dailyClicks, referrers, browsers, os, devices, countries);
    }

    private List<AnalyticsResponse.DataPoint> getDistribution(java.util.stream.Stream<String> stream) {
        Map<String, Long> map = stream
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return map.entrySet().stream()
                .map(e -> new AnalyticsResponse.DataPoint(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get aggregate statistics for all dashboard metrics.
     */
    public DashboardSummary getDashboardSummary() {
        long totalLinks = urlRepository.countByIsDeletedFalse();
        long totalClicks = visitRepository.count();
        long activeLinks = urlRepository.countActiveLinks();
        long expiredLinks = urlRepository.countExpiredLinks();

        return new DashboardSummary(totalLinks, totalClicks, activeLinks, expiredLinks);
    }

    /**
     * Map UrlEntity and its click count to a LinkResponse DTO.
     */
    private LinkResponse convertToResponse(UrlEntity entity, String baseUrl) {
        long clickCount = visitRepository.countByUrlId(entity.getId());
        String absoluteShortUrl = baseUrl + entity.getShortUrl();
        return new LinkResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getFullUrl(),
                absoluteShortUrl,
                entity.getShortUrl(),
                entity.getCreatedAt(),
                entity.getExpiryDate(),
                entity.isEnabled(),
                clickCount
        );
    }
}
