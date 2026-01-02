package com.goatwatches.exception;

public class AuthException extends RuntimeException {
    private final String field;

    public AuthException(String message) {
        super(message);
        this.field = null;
    }

    public AuthException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}