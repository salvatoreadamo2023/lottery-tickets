package com.lottery.tickets.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String ticketId) {
        super("Ticket non trovato con id: " + ticketId);
    }
}