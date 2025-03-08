package com.example.webapp.exceptions;

public class ProductRemovalException extends RuntimeException {
    public ProductRemovalException(String message) {
        super(message);
    }

    public ProductRemovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
