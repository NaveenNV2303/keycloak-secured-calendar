package com.example.calendar.exception;

//Global exception handler
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(CalendarServiceException.class)
 public ResponseEntity<String> handleCalendarServiceException(CalendarServiceException ex) {
     // Log error, create custom error response if needed
     return ResponseEntity
             .status(HttpStatus.INTERNAL_SERVER_ERROR)
             .body("Calendar service error: " + ex.getMessage());
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<String> handleGenericException(Exception ex) {
     // Catch all for other exceptions
     return ResponseEntity
             .status(HttpStatus.INTERNAL_SERVER_ERROR)
             .body("Internal server error: " + ex.getMessage());
 }
}

