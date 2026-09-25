package com.robinttn.supportticketsystem.exception;

import com.robinttn.supportticketsystem.domain.TicketStatus;

public class InvalidStateTransitionException extends RuntimeException {

    private final TicketStatus current;
    private final TicketStatus target;

    public InvalidStateTransitionException(TicketStatus current, TicketStatus target) {
        super("Cannot transition ticket status from %s to %s".formatted(current, target));
        this.current = current;
        this.target = target;
    }

    public TicketStatus getCurrent() {
        return current;
    }

    public TicketStatus getTarget() {
        return target;
    }
}
