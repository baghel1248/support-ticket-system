package com.robinttn.supportticketsystem.controller;

import com.robinttn.supportticketsystem.domain.Priority;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import com.robinttn.supportticketsystem.dto.AddCommentRequest;
import com.robinttn.supportticketsystem.dto.CommentResponse;
import com.robinttn.supportticketsystem.dto.CreateTicketRequest;
import com.robinttn.supportticketsystem.dto.TicketResponse;
import com.robinttn.supportticketsystem.dto.UpdateTicketRequest;
import com.robinttn.supportticketsystem.exception.InvalidStateTransitionException;
import com.robinttn.supportticketsystem.exception.ResourceNotFoundException;
import com.robinttn.supportticketsystem.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void createTicketReturnsCreatedTicket() throws Exception {
        when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(sampleTicket());

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Fix login crash",
                                  "description": "Users get 500 error on submit",
                                  "priority": "HIGH",
                                  "assignee": "alex"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fix login crash"))
                .andExpect(jsonPath("$.description").value("Users get 500 error on submit"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignee").value("alex"));
    }

    @Test
    void listTicketsReturnsTicketArray() throws Exception {
        when(ticketService.getAllTickets(isNull(), isNull())).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix login crash"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void getTicketByIdReturnsNotFoundProblemDetails() throws Exception {
        when(ticketService.getTicketById(404L))
                .thenThrow(new ResourceNotFoundException("Ticket not found: 404"));

        mockMvc.perform(get("/api/v1/tickets/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Ticket not found: 404"));
    }

    @Test
    void malformedJsonReturnsBadRequestProblemDetails() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Malformed JSON request body"));
    }

    @Test
    void listTicketsCombinesStatusAndKeywordFilters() throws Exception {
        when(ticketService.getAllTickets(TicketStatus.OPEN, "login"))
                .thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/api/v1/tickets")
                        .queryParam("status", "OPEN")
                        .queryParam("keyword", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void invalidStatusQueryParameterReturnsBadRequestProblemDetails() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .queryParam("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'status'"));
    }

    @Test
    void updateTicketRejectsWhitespaceOnlyTitleAndDescription() throws Exception {
        mockMvc.perform(put("/api/v1/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "\\t"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void invalidStatusUpdateReturnsBadRequestProblemDetails() throws Exception {
        when(ticketService.updateStatus(eq(1L), eq(TicketStatus.CLOSED)))
                .thenThrow(new InvalidStateTransitionException(TicketStatus.OPEN, TicketStatus.CLOSED));

        mockMvc.perform(patch("/api/v1/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid state transition"))
                .andExpect(jsonPath("$.detail").value("Cannot transition ticket status from OPEN to CLOSED"))
                .andExpect(jsonPath("$.currentStatus").value("OPEN"))
                .andExpect(jsonPath("$.targetStatus").value("CLOSED"));
    }

    @Test
    void addCommentReturnsCreatedComment() throws Exception {
        Instant now = Instant.parse("2026-09-25T07:00:00Z");
        when(ticketService.addComment(eq(1L), any(AddCommentRequest.class)))
                .thenReturn(new CommentResponse(9L, "jane", "Investigating logs.", now));

        mockMvc.perform(post("/api/v1/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "jane",
                                  "content": "Investigating logs."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.author").value("jane"))
                .andExpect(jsonPath("$.content").value("Investigating logs."));
    }

    @Test
    void addCommentReturnsNotFoundWhenTicketDoesNotExist() throws Exception {
        when(ticketService.addComment(eq(404L), any(AddCommentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Ticket not found: 404"));

        mockMvc.perform(post("/api/v1/tickets/404/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "jane",
                                  "content": "Investigating logs."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Ticket not found: 404"));
    }

    @Test
    void updateTicketReturnsUpdatedTicket() throws Exception {
        when(ticketService.updateTicket(eq(1L), any(UpdateTicketRequest.class))).thenReturn(sampleTicket());

        mockMvc.perform(put("/api/v1/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Fix login crash",
                                  "description": "Users get 500 error on submit",
                                  "priority": "HIGH",
                                  "assignee": "alex"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fix login crash"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    private static TicketResponse sampleTicket() {
        Instant now = Instant.parse("2026-09-25T07:00:00Z");
        return new TicketResponse(
                1L,
                "Fix login crash",
                "Users get 500 error on submit",
                TicketStatus.OPEN,
                Priority.HIGH,
                "alex",
                now,
                now,
                0L,
                List.of()
        );
    }
}
