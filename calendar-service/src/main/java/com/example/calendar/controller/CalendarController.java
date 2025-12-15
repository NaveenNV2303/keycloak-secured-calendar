package com.example.calendar.controller;

import com.example.calendar.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for calendar endpoints.
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * Retrieves calendar events.
     * Accessible only to users with role 'my-role'.
     *
     * @return list of calendar events
     */
    @GetMapping
    @PreAuthorize("hasRole('my-role')")
    public ResponseEntity<List<Map<String, Object>>> getCalendar() {
        logger.info("Fetching calendar events");
        // Let exceptions propagate to be handled by GlobalExceptionHandler
        List<Map<String, Object>> events = calendarService.getCalendarEvents();
        return ResponseEntity.ok(events);
    }
}
