package com.robinttn.supportticketsystem.dto;

import com.robinttn.supportticketsystem.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String description,
        Priority priority,
        @Size(max = 100) String assignee
) {
}
