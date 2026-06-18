package com.example.exception;

public class ValidationException extends RuntimeException {

    private final String field;

    public ValidationException(String field, String reason) {
        super("Validation failed for [" + field + "]: " + reason);
        this.field = field;
    }

    public String getField() { return field; }
}
