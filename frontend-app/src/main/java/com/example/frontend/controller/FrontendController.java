package com.example.frontend.controller;

import com.example.frontend.model.CalendarEvent;
import com.example.frontend.service.FrontendService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for rendering frontend pages and handling user requests.
 */
@Controller
public class FrontendController {

    private static final Logger log = LoggerFactory.getLogger(FrontendController.class);

    private final FrontendService frontendService;

    public FrontendController(FrontendService frontendService) {
        this.frontendService = frontendService;
    }

    /**
     * Home page rendering.
     * Adds authenticated user and roles to the model.
     */
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OidcUser user) {
        log.debug("Rendering home page for user: {}", user != null ? user.getPreferredUsername() : "anonymous");
        model.addAttribute("user", user);
        model.addAttribute("roles", extractRoles(user));
        return "index";
    }

    /**
     * Fetches calendar events and renders the calendar view.
     * Handles errors gracefully and logs issues.
     */
    @GetMapping("/calendar")
    public String getCalendar(
            Model model,
            @AuthenticationPrincipal OidcUser user,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {

        // Log incoming request with user info (or anonymous if not authenticated)
        log.debug("Received request for calendar data from user: {}", user != null ? user.getPreferredUsername() : "anonymous");

        // Add user info and roles to the model for frontend rendering
        model.addAttribute("user", user);
        model.addAttribute("roles", extractRoles(user));

        // If user is not authenticated, warn and return empty calendar with message
        if (user == null) {
            log.warn("Unauthorized access attempt to /calendar endpoint.");
            model.addAttribute("calendarEvents", List.of());
            model.addAttribute("calendarData", "User is not authenticated");
            return "index";
        }

        // No try-catch block here to keep controller clean
        // Exceptions from fetchCalendarEvents will be handled globally by GlobalExceptionHandler
        List<CalendarEvent> calendarEvents = frontendService.fetchCalendarEvents(authorizedClient);

        // Log success and add events to model
        log.info("Fetched {} calendar events for user {}", calendarEvents.size(), user.getPreferredUsername());
        model.addAttribute("calendarEvents", calendarEvents);
        model.addAttribute("calendarData", null);

        return "index";
    }

    /**
     * Simple access denied page mapping.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        log.warn("Access denied page requested.");
        return "access-denied";
    }

    /**
     * Extract roles from the authenticated user.
     */
    private List<String> extractRoles(OidcUser user) {
        if (user == null) return List.of();

        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toList());
    }
}
