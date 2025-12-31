package com.example.apps.contacts.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyContactMessageRequest {
    @NotBlank(message = "Reply message cannot be empty")
    private String replyMessage;
}
