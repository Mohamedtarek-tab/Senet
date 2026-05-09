package com.senet.booking.strategy;

public interface StatusTransitionStrategy {
    String transition(String currentStatus, String requestedStatus); // returns new status or throws exception if invalid
}