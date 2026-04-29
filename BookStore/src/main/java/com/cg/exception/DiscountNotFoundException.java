package com.cg.exception;

public class DiscountNotFoundException extends RuntimeException {
    public DiscountNotFoundException(String storId) {
        super("No discounts found for store ID: " + storId);
    }
}
