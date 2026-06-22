package com.carmatch.exception;

public class SessionAlreadyActiveException extends RuntimeException {
    public SessionAlreadyActiveException(String message) {
        super(message);
    }
}