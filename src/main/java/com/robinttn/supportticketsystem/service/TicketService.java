package com.robinttn.supportticketsystem.service;

import com.robinttn.supportticketsystem.domain.Comment;
import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStateTransitionValidator;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import com.robinttn.supportticketsystem.dto.AddCommentRequest;
import com.robinttn.supportticketsystem.dto.CommentResponse;
import com.robinttn.supportticketsystem.dto.CreateTicketRequest;
import com.robinttn.supportticketsystem.dto.TicketResponse;
import com.robinttn.supportticketsystem.dto.UpdateTicketRequest;
import com.robinttn.supportticketsystem.exception.ResourceNotFoundException;
import com.robinttn.supportticketsystem.repository.CommentRepository;
import com.robinttn.supportticketsystem.repository.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketStateTransitionValidator stateTransitionValidator;

    public TicketService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            TicketStateTransitionValidator stateTransitionValidator
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.stateTransitionValidator = stateTransitionValidator;
    }

    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(request.priority() != null ? request.priority() : Priority.LOW);
        ticket.setAssignee(request.assignee());
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketStatus status, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        List<Ticket> tickets;
        if (status != null && hasKeyword) {
            tickets = ticketRepository.searchByKeyword(keyword.trim()).stream()
                    .filter(ticket -> ticket.getStatus() == status)
                    .toList();
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (hasKeyword) {
            tickets = ticketRepository.searchByKeyword(keyword.trim());
        } else {
            tickets = ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        return TicketResponse.from(findTicket(id));
    }

    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = findTicket(id);
        if (request.title() != null) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.assignee() != null) {
            ticket.setAssignee(request.assignee());
        }
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    public TicketResponse updateStatus(Long id, TicketStatus newStatus) {
        Ticket ticket = findTicket(id);
        stateTransitionValidator.validateTransition(ticket.getStatus(), newStatus);
        ticket.setStatus(newStatus);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    public CommentResponse addComment(Long id, AddCommentRequest request) {
        Ticket ticket = findTicket(id);
        Comment comment = new Comment();
        comment.setAuthor(request.author());
        comment.setContent(request.content());
        ticket.addComment(comment);
        return CommentResponse.from(commentRepository.save(comment));
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }
}
