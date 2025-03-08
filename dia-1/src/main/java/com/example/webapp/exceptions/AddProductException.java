package com.example.webapp.exceptions;

public class AddProductException extends RuntimeException {
    public AddProductException(String message) {
        super(message);
    }

    public AddProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
