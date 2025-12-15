package com.example.frontend.exception;

public class FrontendServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FrontendServiceException(String message) {
        super(message);
    }

    public FrontendServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
