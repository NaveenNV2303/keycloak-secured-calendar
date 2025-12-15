package com.example.frontend.service;

import com.example.frontend.exception.FrontendServiceException;  // Custom exception for this service
import com.example.frontend.model.CalendarEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Service responsible for interacting with the calendar backend service.
 */
@Service
public class FrontendService {

    private static final Logger log = LoggerFactory.getLogger(FrontendService.class);

    private final WebClient calendarWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injecting WebClient and ObjectMapper dependencies.
     *
     * @param calendarWebClient WebClient configured for calendar backend
     * @param objectMapper Jackson ObjectMapper for JSON deserialization
     */
    public FrontendService(WebClient calendarWebClient, ObjectMapper objectMapper) {
        this.calendarWebClient = calendarWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches calendar events from the calendar backend service using the provided OAuth2 client.
     * Uses the access token for authentication.
     *
     * @param authorizedClient OAuth2AuthorizedClient containing access token
     * @return List of CalendarEvent objects, or empty list if no events are found
     * @throws FrontendServiceException when HTTP or deserialization errors occur
     */
    public List<CalendarEvent> fetchCalendarEvents(OAuth2AuthorizedClient authorizedClient) {
        // Extract the access token from the authorized client
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        String tokenValue = accessToken.getTokenValue();

        log.debug("Fetching calendar events with access token: [REDACTED]");

        try {
            // Make GET request to /calendar endpoint with Bearer token authentication
            String responseBody = calendarWebClient.get()
                    .uri("/calendar")
                    .headers(headers -> headers.setBearerAuth(tokenValue))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Handle empty or null response gracefully
            if (responseBody == null || responseBody.isBlank()) {
                log.info("No calendar events returned from calendar service.");
                return List.of();
            }

            log.debug("Received calendar events response: {}", responseBody);

            // Deserialize JSON response into list of CalendarEvent objects
            List<CalendarEvent> events = objectMapper.readValue(responseBody, new TypeReference<List<CalendarEvent>>() {});

            log.info("Successfully parsed {} calendar events.", events.size());

            return events;

        } catch (WebClientResponseException e) {
            // Handle HTTP errors separately for better diagnostics
            log.error("HTTP error while fetching calendar events: {}", e.getStatusCode(), e);
            throw new FrontendServiceException("Failed to fetch calendar events due to HTTP error: " + e.getStatusCode(), e);

        } catch (Exception e) {
            // Catch-all for other errors (e.g. JSON parsing)
            log.error("Unexpected error while fetching calendar events", e);
            throw new FrontendServiceException("Unexpected error occurred while fetching calendar events", e);
        }
    }
}
