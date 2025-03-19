package com.example.webapp.exceptions;

public class PermissionUpdateException extends RuntimeException {
    public PermissionUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}