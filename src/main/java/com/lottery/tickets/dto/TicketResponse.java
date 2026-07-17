package com.lottery.tickets.dto;

import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;

import java.time.LocalDateTime;

public class TicketResponse {

	private String ticketId;
	private String privateCode;
	private Status status;
	private LocalDateTime extractAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static TicketResponse fromEntity(Ticket ticket) {
		TicketResponse response = new TicketResponse();
		response.ticketId = ticket.getTicketId();
		response.privateCode = ticket.getPrivateCode();
		response.status = ticket.getStatus();
		response.extractAt = ticket.getExtractAt();
		response.createdAt = ticket.getCreatedAt();
		response.updatedAt = ticket.getUpdatedAt();
		return response;
	}

	public String getTicketId() {
		return ticketId;
	}

	public String getPrivateCode() {
		return privateCode;
	}

	public Status getStatus() {
		return status;
	}

	public LocalDateTime getExtractAt() {
		return extractAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}