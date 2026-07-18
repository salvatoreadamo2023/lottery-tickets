package com.lottery.tickets.dto;

import com.lottery.tickets.entity.Status;
import jakarta.validation.constraints.NotNull;

public class TicketStatusUpdateRequest {

    @NotNull(message = "status è obbligatorio")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}