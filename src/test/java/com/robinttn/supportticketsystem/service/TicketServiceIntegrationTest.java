package com.robinttn.supportticketsystem.service;

import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import com.robinttn.supportticketsystem.dto.CreateTicketRequest;
import com.robinttn.supportticketsystem.dto.TicketResponse;
import com.robinttn.supportticketsystem.exception.InvalidStateTransitionException;
import com.robinttn.supportticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TicketServiceIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void createTicketPersistsOpenTicket() {
        TicketResponse created = ticketService.createTicket(new CreateTicketRequest(
                "Fix login crash",
                "Users get 500 error on submit",
                Priority.HIGH,
                "alex"
        ));

        Ticket persisted = ticketRepository.findById(created.id()).orElseThrow();
        assertEquals(TicketStatus.OPEN, persisted.getStatus());
        assertEquals("Fix login crash", persisted.getTitle());
    }

    @Test
    void invalidStatusUpdateRollsBackPersistedStatus() {
        TicketResponse created = ticketService.createTicket(new CreateTicketRequest(
                "Printer offline",
                "Network timeout in billing",
                Priority.MEDIUM,
                "jane"
        ));

        assertThrows(InvalidStateTransitionException.class,
                () -> ticketService.updateStatus(created.id(), TicketStatus.CLOSED));

        Ticket persisted = ticketRepository.findById(created.id()).orElseThrow();
        assertEquals(TicketStatus.OPEN, persisted.getStatus());
    }
}
