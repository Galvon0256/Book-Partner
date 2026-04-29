package com.cg.exception;

public class SalesNotFoundException extends RuntimeException {
    public SalesNotFoundException(String id) {
        super("No sales records found for: " + id);
    }
}
