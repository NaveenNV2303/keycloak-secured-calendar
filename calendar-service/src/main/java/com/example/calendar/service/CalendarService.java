package com.example.calendar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.calendar.exception.CalendarServiceException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service responsible for generating calendar events.
 * Simulates event data with random tasks and timestamps.
 */
@Service
public class CalendarService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final List<String> TASKS = List.of(
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

    private static final Random RANDOM = new Random();

    /**
     * Generates a list of calendar events with random titles and times.
     * Events are scheduled within the next 72 hours, rounded to nearest 15 minutes.
     *
     * @return list of event data maps
     */
    public List<Map<String, Object>> getCalendarEvents() {
        logger.info("Generating calendar events");
        try {
            int count = 3 + RANDOM.nextInt(4);  // Generate between 3 and 6 events
            List<Map<String, Object>> data = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                String task = TASKS.get(RANDOM.nextInt(TASKS.size()));
                LocalDateTime time = LocalDateTime.now().plusMinutes(RANDOM.nextInt(72 * 60));
                time = roundToNearestQuarterHour(time);

                Map<String, Object> event = new HashMap<>();
                event.put("title", task);
                event.put("time", FORMATTER.format(time));
                event.put("dateTime", time);  // Used internally for sorting

                data.add(event);
                logger.debug("Created event: title='{}', time='{}'", task, FORMATTER.format(time));
            }

            // Sort events by date/time ascending
            data.sort(Comparator.comparing(event -> (LocalDateTime) event.get("dateTime")));
            logger.info("Sorted {} events by date/time", data.size());

            // Assign sequential IDs and remove internal field
            for (int i = 0; i < data.size(); i++) {
                data.get(i).put("id", i + 1);
                data.get(i).remove("dateTime");
            }

            logger.info("Assigned IDs and finalized event list");
            return data;
        } catch (Exception ex) {
            logger.error("Failed to generate calendar events", ex);
            throw new CalendarServiceException("Error occurred while generating calendar events", ex);
        }
    }
    /**
     * Rounds a LocalDateTime to the nearest quarter hour (15 minutes).
     *
     * @param dateTime input time
     * @return rounded time
     */
    private LocalDateTime roundToNearestQuarterHour(LocalDateTime dateTime) {
        int minutes = dateTime.getMinute();
        int mod = minutes % 15;
        LocalDateTime rounded;
        if (mod < 8) {
            rounded = dateTime.minusMinutes(mod);
        } else {
            rounded = dateTime.plusMinutes(15 - mod);
        }
        logger.debug("Rounded time from {} to {}", dateTime, rounded);
        return rounded;
    }
}
