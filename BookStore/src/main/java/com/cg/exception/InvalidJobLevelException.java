package com.cg.exception;

public class InvalidJobLevelException extends RuntimeException {
    public InvalidJobLevelException(String message) {
        super(message);
    }
}