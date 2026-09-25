package com.robinttn.supportticketsystem.controller;

import com.robinttn.supportticketsystem.domain.TicketStatus;
import com.robinttn.supportticketsystem.dto.AddCommentRequest;
import com.robinttn.supportticketsystem.dto.CommentResponse;
import com.robinttn.supportticketsystem.dto.CreateTicketRequest;
import com.robinttn.supportticketsystem.dto.TicketResponse;
import com.robinttn.supportticketsystem.dto.UpdateStatusRequest;
import com.robinttn.supportticketsystem.dto.UpdateTicketRequest;
import com.robinttn.supportticketsystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping
    public List<TicketResponse> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String keyword
    ) {
        return ticketService.getAllTickets(status, keyword);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PutMapping("/{id}")
    public TicketResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.updateTicket(id, request);
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ticketService.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request
    ) {
        return ticketService.addComment(id, request);
    }
}
