package com.robinttn.supportticketsystem.domain;

import com.robinttn.supportticketsystem.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketStateTransitionValidatorTest {

    private final TicketStateTransitionValidator validator = new TicketStateTransitionValidator();

    @ParameterizedTest
    @CsvSource({
            "OPEN, OPEN",
            "OPEN, IN_PROGRESS",
            "OPEN, CANCELLED",
            "IN_PROGRESS, IN_PROGRESS",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CANCELLED",
            "RESOLVED, RESOLVED",
            "RESOLVED, CLOSED",
            "CLOSED, CLOSED",
            "CANCELLED, CANCELLED"
    })
    void allowedTransitionsCompleteWithoutException(TicketStatus current, TicketStatus target) {
        assertDoesNotThrow(() -> validator.validateTransition(current, target));
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN, RESOLVED",
            "OPEN, CLOSED",
            "IN_PROGRESS, OPEN",
            "IN_PROGRESS, CLOSED",
            "RESOLVED, OPEN",
            "RESOLVED, IN_PROGRESS",
            "RESOLVED, CANCELLED",
            "CLOSED, OPEN",
            "CLOSED, IN_PROGRESS",
            "CLOSED, RESOLVED",
            "CLOSED, CANCELLED",
            "CANCELLED, OPEN",
            "CANCELLED, IN_PROGRESS",
            "CANCELLED, RESOLVED",
            "CANCELLED, CLOSED"
    })
    void forbiddenTransitionsThrowInvalidStateTransitionException(TicketStatus current, TicketStatus target) {
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(current, target));
    }

    @Test
    void nullStatusesThrowInvalidStateTransitionException() {
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(null, TicketStatus.OPEN));
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(TicketStatus.OPEN, null));
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(null, null));
    }
}
