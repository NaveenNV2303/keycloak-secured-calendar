package com.example.frontend.service;

import com.example.frontend.exception.FrontendServiceException;
import com.example.frontend.model.CalendarEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FrontendServiceTest {

    @Mock
    private WebClient calendarWebClient;

    // Use raw types to avoid generic capture issues
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    @Spy
    private ObjectMapper objectMapper;

    private FrontendService frontendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new JavaTimeModule());
        frontendService = new FrontendService(calendarWebClient, objectMapper);
    }

    @Test
    void fetchCalendarEvents_shouldReturnEventsSuccessfully() throws Exception {
        // Sample JSON response
        String jsonResponse = "[{\"id\":1,\"title\":\"Test Event\",\"time\":\"2025-12-15T10:00:00.000\"}]";

        // Mock OAuth2 access token retrieval
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("dummy-token");

        // Mock the WebClient fluent API calls
        when(calendarWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        // Call the method under test
        List<CalendarEvent> events = frontendService.fetchCalendarEvents(authorizedClient);

        // Assertions
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("Test Event", events.get(0).getTitle());
    }

    @Test
    void fetchCalendarEvents_shouldReturnEmptyListForEmptyResponse() throws Exception {
        // Mock OAuth2 access token retrieval
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("dummy-token");

        // Mock the WebClient fluent API calls returning empty response
        when(calendarWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(""));

        List<CalendarEvent> events = frontendService.fetchCalendarEvents(authorizedClient);

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void fetchCalendarEvents_shouldThrowFrontendServiceExceptionOnHttpError() {
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("dummy-token");

        when(calendarWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Simulate HTTP error
        when(responseSpec.bodyToMono(String.class))
            .thenThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null));

        FrontendServiceException exception = assertThrows(FrontendServiceException.class, () ->
            frontendService.fetchCalendarEvents(authorizedClient)
        );

        assertTrue(exception.getMessage().contains("Failed to fetch calendar events due to HTTP error"));
    }

    @Test
    void fetchCalendarEvents_shouldThrowFrontendServiceExceptionOnOtherError() throws Exception {
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("dummy-token");

        when(calendarWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Simulate successful call returning invalid JSON to cause deserialization error
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("invalid-json"));

        FrontendServiceException exception = assertThrows(FrontendServiceException.class, () ->
            frontendService.fetchCalendarEvents(authorizedClient)
        );

        assertTrue(exception.getMessage().contains("Unexpected error occurred while fetching calendar events"));
    }
}
