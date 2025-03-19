package com.example.webapp.exceptions;

public class InvalidCalculationException extends RuntimeException {
    public InvalidCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
