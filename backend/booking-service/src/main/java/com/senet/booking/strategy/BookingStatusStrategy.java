package com.senet.booking.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class BookingStatusStrategy implements StatusTransitionStrategy {

    // Defines which transitions are legal from each state
    private static final Map<String, Set<String>> ALLOWED = Map.of(
        "pending",   Set.of("confirmed", "cancelled"),
        "confirmed", Set.of("completed", "cancelled"),
        "completed", Set.of(),          // terminal — no transitions allowed
        "cancelled", Set.of()           // terminal — no transitions allowed
    );

    @Override
    public String transition(String currentStatus, String requestedStatus) {
        Set<String> allowed = ALLOWED.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(requestedStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from '%s' to '%s'", currentStatus, requestedStatus));
        }
        return requestedStatus;
    }
}