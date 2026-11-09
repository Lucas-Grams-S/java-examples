package com.example.orders.shared;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
