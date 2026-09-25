package com.robinttn.supportticketsystem.dto;

import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        String assignee,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        List<CommentResponse> comments
) {

    public static TicketResponse from(Ticket ticket) {
        List<CommentResponse> comments = ticket.getComments() == null
                ? List.of()
                : ticket.getComments().stream().map(CommentResponse::from).toList();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getVersion(),
                comments
        );
    }
}
