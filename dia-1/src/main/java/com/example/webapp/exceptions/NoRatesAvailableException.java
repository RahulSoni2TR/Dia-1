package com.example.webapp.exceptions;

public class NoRatesAvailableException extends RuntimeException {
    public NoRatesAvailableException(String message) {
        super(message);
    }
}