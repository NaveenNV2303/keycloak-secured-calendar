package com.example.calendar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalendarServiceTest {

    private CalendarService calendarService;

    @BeforeEach
    void setup() {
        calendarService = new CalendarService();
    }

    @Test
    void shouldReturnBetween3And6EventsWithRequiredFields() {
        List<Map<String, Object>> events = calendarService.getCalendarEvents();

        assertNotNull(events, "Events list should not be null");
        assertTrue(events.size() >= 3 && events.size() <= 6,
            "Event count should be between 3 and 6 but was " + events.size());

        for (Map<String, Object> event : events) {
            assertTrue(event.containsKey("id"), "Event missing 'id' key");
            assertTrue(event.containsKey("title"), "Event missing 'title' key");
            assertTrue(event.containsKey("time"), "Event missing 'time' key");
            assertFalse(event.containsKey("dateTime"), "'dateTime' should be removed from event");
        }
    }

    @Test
    void shouldReturnEventsSortedByTimeAndHaveSequentialIds() {
        List<Map<String, Object>> events = calendarService.getCalendarEvents();

        LocalDateTime previousTime = null;
        int previousId = 0;

        for (Map<String, Object> event : events) {
            int id = (int) event.get("id");
            assertTrue(id > previousId, "Event IDs should be sequential and increasing");

            String timeStr = (String) event.get("time");
            LocalDateTime eventTime = LocalDateTime.parse(timeStr);

            if (previousTime != null) {
                assertFalse(eventTime.isBefore(previousTime), "Events should be sorted by ascending time");
            }

            previousTime = eventTime;
            previousId = id;
        }
    }

    @Test
    void shouldReturnEventTitlesFromDefinedTaskList() {
        List<Map<String, Object>> events = calendarService.getCalendarEvents();

        List<String> expectedTasks = List.of(
            "Team meeting",
            "Doctor appointment",
            "Project review",
            "Client call",
            "One-on-one meeting",
            "Lunch with team",
            "Code review",
            "Product demo",
            "Client feedback session",
            "Design brainstorming"
        );

        for (Map<String, Object> event : events) {
            String title = (String) event.get("title");
            assertTrue(expectedTasks.contains(title), "Event title '" + title + "' is not in the expected tasks list");
        }
    }

    @Test
    void shouldReturnEventTimesWithinNext72Hours() {
        List<Map<String, Object>> events = calendarService.getCalendarEvents();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxTime = now.plusHours(72);

        for (Map<String, Object> event : events) {
            String timeStr = (String) event.get("time");
            LocalDateTime eventTime = LocalDateTime.parse(timeStr);

            assertFalse(eventTime.isBefore(now.minusMinutes(1)), "Event time should not be before now");
            assertFalse(eventTime.isAfter(maxTime.plusMinutes(1)), "Event time should be within next 72 hours");
        }
    }

    @Test
    void shouldNeverReturnNullOrEmptyEventsList() {
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> events = calendarService.getCalendarEvents();
            assertNotNull(events, "Events list should not be null");
            assertFalse(events.isEmpty(), "Events list should never be empty");
        }
    }

    @Test
    void shouldRoundToNearestQuarterHourCorrectly() throws Exception {
        Method roundMethod = CalendarService.class.getDeclaredMethod("roundToNearestQuarterHour", LocalDateTime.class);
        roundMethod.setAccessible(true);

        // Cases < 8 minutes rounds down
        LocalDateTime input = LocalDateTime.of(2023, 1, 1, 10, 7);
        LocalDateTime result = (LocalDateTime) roundMethod.invoke(calendarService, input);
        assertEquals(0, result.getMinute() % 15, "Rounded minutes should be multiple of 15");
        assertTrue(!result.isAfter(input), "Rounded time should be before or equal to input if minutes < 8");

        // Cases >= 8 minutes rounds up
        input = LocalDateTime.of(2023, 1, 1, 10, 8);
        result = (LocalDateTime) roundMethod.invoke(calendarService, input);
        assertEquals(0, result.getMinute() % 15, "Rounded minutes should be multiple of 15");
        assertTrue(!result.isBefore(input), "Rounded time should be after or equal to input if minutes >= 8");

        // Test exact multiples don't change
        input = LocalDateTime.of(2023, 1, 1, 10, 15);
        result = (LocalDateTime) roundMethod.invoke(calendarService, input);
        assertEquals(input, result, "Exact quarter hour should remain unchanged");

        // Test edge case 0 minutes
        input = LocalDateTime.of(2023, 1, 1, 10, 0);
        result = (LocalDateTime) roundMethod.invoke(calendarService, input);
        assertEquals(input, result, "Zero minutes should remain unchanged");
    }

}
