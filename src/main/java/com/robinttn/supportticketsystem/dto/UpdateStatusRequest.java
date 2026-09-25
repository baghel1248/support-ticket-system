package com.robinttn.supportticketsystem.dto;

import com.robinttn.supportticketsystem.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull TicketStatus status
) {
}
