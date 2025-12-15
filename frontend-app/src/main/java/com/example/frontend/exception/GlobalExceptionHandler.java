package com.example.frontend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 * Catches and handles exceptions thrown by controllers and services.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles FrontendServiceException exceptions.
     *
     * @param ex the FrontendServiceException thrown
     * @return a ResponseEntity with error message and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(FrontendServiceException.class)
    public ResponseEntity<String> handleCalendarServiceException(FrontendServiceException ex) {
        log.error("FrontendServiceException caught: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Calendar service error: " + ex.getMessage());
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex the Exception thrown
     * @return a ResponseEntity with a generic error message and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected exception caught: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }
}
