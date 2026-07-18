package com.lottery.tickets.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public class TicketRequest {

    @NotNull(message = "extractAt è obbligatorio")
    @Future(message = "extractAt deve essere una data futura")
    private LocalDateTime extractAt;

    public LocalDateTime getExtractAt() {
        return extractAt;
    }

    public void setExtractAt(LocalDateTime extractAt) {
        this.extractAt = extractAt;
    }
}