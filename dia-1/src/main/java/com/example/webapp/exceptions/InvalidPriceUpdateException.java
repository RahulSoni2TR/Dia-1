package com.example.webapp.exceptions;

public class InvalidPriceUpdateException extends RuntimeException {
    public InvalidPriceUpdateException(String message) {
        super(message);
    }
}