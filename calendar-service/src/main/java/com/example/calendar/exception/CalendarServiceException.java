package com.example.calendar.exception;

/**
 * Custom exception for calendar service errors.
 */
public class CalendarServiceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
    public CalendarServiceException(String message) {
        super(message);
    }

    public CalendarServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
