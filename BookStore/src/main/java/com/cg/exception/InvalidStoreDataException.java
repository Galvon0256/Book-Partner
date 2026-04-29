package com.cg.exception;

public class InvalidStoreDataException extends RuntimeException {
    public InvalidStoreDataException(String message) {
        super(message);
    }
}
