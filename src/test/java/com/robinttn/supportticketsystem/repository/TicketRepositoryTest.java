package com.robinttn.supportticketsystem.repository;

import com.robinttn.supportticketsystem.domain.Comment;
import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesTicketWithCommentsAndRetrievesThem() {
        Ticket ticket = newTicket("Cannot reset password", "Reset email never arrives", TicketStatus.OPEN);
        Comment comment = newComment("alex", "Checking the mail gateway");
        ticket.addComment(comment);

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        entityManager.clear();

        Ticket found = ticketRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Cannot reset password", found.getTitle());
        assertEquals(TicketStatus.OPEN, found.getStatus());
        assertEquals(Priority.HIGH, found.getPriority());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
        assertNotNull(found.getVersion());
        assertEquals(1, found.getComments().size());
        assertEquals("alex", found.getComments().getFirst().getAuthor());
        assertEquals("Checking the mail gateway", found.getComments().getFirst().getContent());
        assertNotNull(found.getComments().getFirst().getCreatedAt());
    }

    @Test
    void searchByKeywordMatchesTitleOrDescriptionIgnoreCase() {
        ticketRepository.save(newTicket("Login crash on submit", "Users get a 500 error", TicketStatus.OPEN));
        ticketRepository.save(newTicket("Printer offline", "Network timeout in billing", TicketStatus.OPEN));
        ticketRepository.flush();
        entityManager.clear();

        List<Ticket> byTitle = ticketRepository.searchByKeyword("LOGIN");
        List<Ticket> byDescription = ticketRepository.searchByKeyword("billing");

        assertEquals(1, byTitle.size());
        assertEquals("Login crash on submit", byTitle.getFirst().getTitle());
        assertEquals(1, byDescription.size());
        assertEquals("Printer offline", byDescription.getFirst().getTitle());
        assertTrue(ticketRepository.searchByKeyword("unrelated").isEmpty());
    }

    @Test
    void findByStatusFiltersTickets() {
        ticketRepository.save(newTicket("Open item", "Needs triage", TicketStatus.OPEN));
        Ticket inProgress = newTicket("Active item", "Being fixed", TicketStatus.IN_PROGRESS);
        ticketRepository.save(inProgress);
        ticketRepository.flush();
        entityManager.clear();

        List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
        List<Ticket> inProgressTickets = ticketRepository.findByStatus(TicketStatus.IN_PROGRESS);

        assertEquals(1, openTickets.size());
        assertEquals("Open item", openTickets.getFirst().getTitle());
        assertEquals(1, inProgressTickets.size());
        assertEquals("Active item", inProgressTickets.getFirst().getTitle());
        assertTrue(ticketRepository.findByStatus(TicketStatus.CLOSED).isEmpty());
    }

    private static Ticket newTicket(String title, String description, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(Priority.HIGH);
        ticket.setAssignee("alex");
        return ticket;
    }

    private static Comment newComment(String author, String content) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setContent(content);
        return comment;
    }
}
