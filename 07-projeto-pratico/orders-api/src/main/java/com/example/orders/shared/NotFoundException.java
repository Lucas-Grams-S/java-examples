package com.example.orders.shared;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String what) { super(what + " not found"); }
}
