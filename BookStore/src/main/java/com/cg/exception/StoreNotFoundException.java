package com.cg.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(String storId) {
        super("Store not found with ID: " + storId);
    }
}
