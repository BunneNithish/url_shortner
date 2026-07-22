package com.neueda.interview.urlshortener.controller;

import com.neueda.interview.urlshortener.common.UrlUtil;
import com.neueda.interview.urlshortener.dto.*;
import com.neueda.interview.urlshortener.error.InvalidUrlError;
import com.neueda.interview.urlshortener.model.UrlEntity;
import com.neueda.interview.urlshortener.service.UrlService;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * Helper to compute absolute base URL (domain + port) of the backend.
     */
    private String getBaseUrl(HttpServletRequest request) {
        try {
            return UrlUtil.getBaseUrl(request.getRequestURL().toString());
        } catch (MalformedURLException e) {
            logger.error("Malformed request URL for base calculation", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request URL is invalid", e);
        }
    }

    /**
     * Legacy shorten endpoint (POST /shorten) - preserved for backward compatibility and tests.
     */
    @PostMapping("/shorten")
    public ResponseEntity<Object> saveUrl(@RequestBody FullUrl fullUrl, HttpServletRequest request) {
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        String url = fullUrl.getFullUrl();
        if (url == null || !validator.isValid(url)) {
            logger.error("Malformed URL provided to legacy /shorten");
            InvalidUrlError error = new InvalidUrlError("url", url, "Invalid URL");
            return ResponseEntity.badRequest().body(error);
        }

        String baseUrl = getBaseUrl(request);
        CreateLinkRequest createRequest = new CreateLinkRequest();
        createRequest.setFullUrl(url);
        
        LinkResponse response = urlService.createLink(createRequest, baseUrl);
        
        // Return ShortUrl DTO matching the legacy format
        ShortUrl shortUrl = new ShortUrl(response.getShortUrl());
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * Redirect endpoint (GET /{shortenString}) - Redirects to full URL and logs analytics.
     */
    @GetMapping("/{shortenString}")
    public void redirectToFullUrl(HttpServletResponse response, HttpServletRequest request, 
                                  @PathVariable String shortenString) {
        try {
            UrlEntity entity = urlService.getActiveUrlEntity(shortenString);
            
            // Record analytics asynchronously or safely
            try {
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                String referrer = request.getHeader("Referer"); // HTTP spelling is Referer
                
                urlService.recordVisit(entity.getId(), ipAddress, userAgent, referrer);
            } catch (Exception e) {
                logger.error("Failed to log visit analytics", e);
            }

            logger.info("Redirecting to: {}", entity.getFullUrl());
            response.sendRedirect(entity.getFullUrl());
            
        } catch (ResponseStatusException e) {
            logger.error("Redirect error: status={}, reason={}", e.getStatusCode(), e.getReason());
            throw e;
        } catch (NoSuchElementException e) {
            logger.error("No URL found for short code {}", shortenString);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found", e);
        } catch (IOException e) {
            logger.error("Could not redirect to full URL", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Redirect failed", e);
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                         Dashboard API Endpoints                            */
    /* -------------------------------------------------------------------------- */

    /**
     * Create short link with full metadata (POST /api/links)
     */
    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> createLink(@RequestBody CreateLinkRequest createRequest, 
                                                   HttpServletRequest request) {
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        if (createRequest.getFullUrl() == null || !validator.isValid(createRequest.getFullUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid HTTP or HTTPS URL is required");
        }

        String baseUrl = getBaseUrl(request);
        LinkResponse link = urlService.createLink(createRequest, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    /**
     * Get paginated and searchable list of links (GET /api/links)
     */
    @GetMapping("/api/links")
    public ResponseEntity<Page<LinkResponse>> getLinks(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            HttpServletRequest request) {

        String baseUrl = getBaseUrl(request);
        
        // Parse sort query e.g., "createdAt,desc"
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<LinkResponse> links = urlService.searchLinks(search, pageable, baseUrl);
        return ResponseEntity.ok(links);
    }

    /**
     * Get details of a single link (GET /api/links/{id})
     */
    @GetMapping("/api/links/{id}")
    public ResponseEntity<LinkResponse> getLinkDetails(@PathVariable Long id, HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        LinkResponse link = urlService.getLinkDetails(id, baseUrl);
        return ResponseEntity.ok(link);
    }

    /**
     * Update an existing link's details (PUT /api/links/{id})
     */
    @PutMapping("/api/links/{id}")
    public ResponseEntity<LinkResponse> updateLink(@PathVariable Long id, 
                                                   @RequestBody UpdateLinkRequest updateRequest, 
                                                   HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        LinkResponse link = urlService.updateLink(id, updateRequest, baseUrl);
        return ResponseEntity.ok(link);
    }

    /**
     * Soft delete a link (DELETE /api/links/{id})
     */
    @DeleteMapping("/api/links/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable Long id) {
        urlService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get click analytics for a single link (GET /api/links/{id}/analytics)
     */
    @GetMapping("/api/links/{id}/analytics")
    public ResponseEntity<AnalyticsResponse> getLinkAnalytics(@PathVariable Long id) {
        AnalyticsResponse analytics = urlService.getLinkAnalytics(id);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get global metrics dashboard summary counts (GET /api/analytics/summary)
     */
    @GetMapping("/api/analytics/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        DashboardSummary summary = urlService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
