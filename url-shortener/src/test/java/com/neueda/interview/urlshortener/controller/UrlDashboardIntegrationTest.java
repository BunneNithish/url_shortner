package com.neueda.interview.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neueda.interview.urlshortener.dto.CreateLinkRequest;
import com.neueda.interview.urlshortener.dto.UpdateLinkRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UrlDashboardIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateShortLinkWithCustomAliasAndExpiry() throws Exception {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setFullUrl("https://example.com/dashboard-test");
        request.setTitle("Dashboard Test");
        request.setCustomAlias("dash-test-alias");
        request.setExpiryDate(LocalDateTime.now().plusDays(5));

        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Dashboard Test")))
                .andExpect(jsonPath("$.shortCode", is("dash-test-alias")))
                .andExpect(jsonPath("$.expiryDate").exists())
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.clickCount", is(0)));
    }

    @Test
    public void shouldFailWhenCustomAliasAlreadyExists() throws Exception {
        CreateLinkRequest request1 = new CreateLinkRequest();
        request1.setFullUrl("https://example.com/one");
        request1.setCustomAlias("duplicate-alias");

        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CreateLinkRequest request2 = new CreateLinkRequest();
        request2.setFullUrl("https://example.com/two");
        request2.setCustomAlias("duplicate-alias");

        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldListAndSearchShortLinksWithPagination() throws Exception {
        CreateLinkRequest req1 = new CreateLinkRequest();
        req1.setFullUrl("https://google.com");
        req1.setTitle("Google Search Engine");
        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        CreateLinkRequest req2 = new CreateLinkRequest();
        req2.setFullUrl("https://yahoo.com");
        req2.setTitle("Yahoo Main Page");
        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        mvc.perform(get("/api/links?search=google&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", containsString("Google")));
    }

    @Test
    public void shouldRedirectAndTrackVisitAnalytics() throws Exception {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setFullUrl("https://github.com");
        request.setTitle("GitHub Homepage");
        request.setCustomAlias("gh-link");

        mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Perform Redirect
        mvc.perform(get("/gh-link")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://news.ycombinator.com"))
                .andExpect(status().is3xxRedirection());

        // Get Summary
        mvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks", greaterThanOrEqualTo(1)));
    }

    @Test
    public void shouldUpdateAndSoftDeleteLink() throws Exception {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setFullUrl("https://wikipedia.org");
        request.setTitle("Wikipedia");
        request.setCustomAlias("wiki");

        String response = mvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Update link
        UpdateLinkRequest updateRequest = new UpdateLinkRequest();
        updateRequest.setTitle("Wikipedia Refreshed");
        updateRequest.setEnabled(false);

        mvc.perform(put("/api/links/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Wikipedia Refreshed")))
                .andExpect(jsonPath("$.enabled", is(false)));

        // Soft delete link
        mvc.perform(delete("/api/links/" + id))
                .andExpect(status().isNoContent());

        // Try to get details - should be 404 since it's soft deleted
        mvc.perform(get("/api/links/" + id))
                .andExpect(status().isNotFound());
    }
}
