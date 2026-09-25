package com.robinttn.supportticketsystem.service;

import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStateTransitionValidator;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import com.robinttn.supportticketsystem.dto.CreateTicketRequest;
import com.robinttn.supportticketsystem.dto.TicketResponse;
import com.robinttn.supportticketsystem.exception.InvalidStateTransitionException;
import com.robinttn.supportticketsystem.exception.ResourceNotFoundException;
import com.robinttn.supportticketsystem.repository.CommentRepository;
import com.robinttn.supportticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketStateTransitionValidator stateTransitionValidator;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicketSavesNewTicketWithOpenStatus() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Fix login crash",
                "Users get 500 error on submit",
                Priority.HIGH,
                "alex"
        );
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ReflectionTestUtils.setField(ticket, "id", 1L);
            return ticket;
        });

        TicketResponse response = ticketService.createTicket(request);

        assertEquals(1L, response.id());
        assertEquals("Fix login crash", response.title());
        assertEquals("Users get 500 error on submit", response.description());
        assertEquals(TicketStatus.OPEN, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals("alex", response.assignee());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicketDefaultsPriorityToLowWhenOmitted() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Need help",
                "Cannot print invoices",
                null,
                null
        );
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.createTicket(request);

        assertEquals(TicketStatus.OPEN, response.status());
        assertEquals(Priority.LOW, response.priority());
    }

    @Test
    void updateStatusInvokesStateValidatorAndUpdatesStatus() {
        Ticket ticket = openTicket(10L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketResponse response = ticketService.updateStatus(10L, TicketStatus.IN_PROGRESS);

        verify(stateTransitionValidator).validateTransition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        verify(ticketRepository).save(ticket);
        assertEquals(TicketStatus.IN_PROGRESS, response.status());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
    }

    @Test
    void updateStatusDoesNotPersistWhenTransitionIsInvalid() {
        Ticket ticket = openTicket(10L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        doThrow(new InvalidStateTransitionException(TicketStatus.OPEN, TicketStatus.CLOSED))
                .when(stateTransitionValidator)
                .validateTransition(TicketStatus.OPEN, TicketStatus.CLOSED);

        assertThrows(InvalidStateTransitionException.class,
                () -> ticketService.updateStatus(10L, TicketStatus.CLOSED));

        verify(stateTransitionValidator).validateTransition(TicketStatus.OPEN, TicketStatus.CLOSED);
        verify(ticketRepository, never()).save(any(Ticket.class));
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    void getTicketByIdThrowsResourceNotFoundForMissingTicket() {
        when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.getTicketById(404L)
        );

        assertEquals("Ticket not found: 404", exception.getMessage());
    }

    @Test
    void getAllTicketsCombinesStatusAndKeywordFilters() {
        Ticket matchingOpenTicket = openTicket(1L);
        Ticket nonMatchingClosedTicket = openTicket(2L);
        nonMatchingClosedTicket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.searchByKeyword("login"))
                .thenReturn(List.of(matchingOpenTicket, nonMatchingClosedTicket));

        List<TicketResponse> responses = ticketService.getAllTickets(TicketStatus.OPEN, " login ");

        assertEquals(1, responses.size());
        assertEquals(1L, responses.getFirst().id());
        assertEquals(TicketStatus.OPEN, responses.getFirst().status());
        verify(ticketRepository).searchByKeyword("login");
    }

    private static Ticket openTicket(Long id) {
        Ticket ticket = new Ticket();
        ReflectionTestUtils.setField(ticket, "id", id);
        ticket.setTitle("Fix login crash");
        ticket.setDescription("Users get 500 error on submit");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(Priority.HIGH);
        ticket.setAssignee("alex");
        return ticket;
    }
}
