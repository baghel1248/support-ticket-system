package com.robinttn.supportticketsystem.domain;

import com.robinttn.supportticketsystem.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TicketStateTransitionValidator {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED, Set.of(),
            TicketStatus.CANCELLED, Set.of()
    );

    public void validateTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new InvalidStateTransitionException(currentStatus, newStatus);
        }
        if (currentStatus == newStatus) {
            return;
        }
        Set<TicketStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTargets.contains(newStatus)) {
            throw new InvalidStateTransitionException(currentStatus, newStatus);
        }
    }
}
