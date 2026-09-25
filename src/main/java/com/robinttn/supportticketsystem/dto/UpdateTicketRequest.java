package com.robinttn.supportticketsystem.dto;

import com.robinttn.supportticketsystem.domain.Priority;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @Size(min = 1, max = 150)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String title,
        @Size(min = 1, max = 2000)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String description,
        Priority priority,
        @Size(max = 100) String assignee
) {
}
