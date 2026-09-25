package com.robinttn.supportticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @NotBlank @Size(min = 1, max = 100) String author,
        @NotBlank @Size(max = 1000) String content
) {
}
