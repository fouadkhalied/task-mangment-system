package com.company.app.modules.taskCore.presentation.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    String error;
    String message;
    int status;
    String path;
    LocalDateTime timestamp;
}
