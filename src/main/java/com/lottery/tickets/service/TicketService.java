package com.lottery.tickets.service;

import com.lottery.tickets.dto.KpiResponse;
import com.lottery.tickets.dto.TicketRequest;
import com.lottery.tickets.dto.TicketResponse;
import com.lottery.tickets.dto.TicketStatusUpdateRequest;
import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;
import com.lottery.tickets.entity.TicketAudit;
import com.lottery.tickets.exception.InvalidStatusException;
import com.lottery.tickets.exception.TicketNotFoundException;
import com.lottery.tickets.repository.TicketAuditRepository;
import com.lottery.tickets.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketAuditRepository ticketAuditRepository;

    public TicketService(TicketRepository ticketRepository, TicketAuditRepository ticketAuditRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketAuditRepository = ticketAuditRepository;
    }

    public TicketResponse createTicket(TicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setPrivateCode(generatePrivateCode());
        ticket.setStatus(Status.CREATO);
        ticket.setExtractAt(request.getExtractAt());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        saveAudit(saved.getTicketId(), null, saved.getStatus().name(), "CREATE", "USER");

        return TicketResponse.fromEntity(saved);
    }

    public TicketResponse getTicket(String ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        return TicketResponse.fromEntity(ticket);
    }

   /* public List<TicketResponse> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        List<TicketResponse> responses = new ArrayList<>();
        for (Ticket ticket : tickets) {
            responses.add(TicketResponse.fromEntity(ticket));
        }
        return responses;
    }*/
    public List<TicketResponse> getAllTickets(Status status, LocalDateTime from, LocalDateTime to) {
        List<Ticket> tickets;

        if (status != null && from != null && to != null) {
            tickets = ticketRepository.findByStatusAndCreatedAtBetween(status, from, to);
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (from != null && to != null) {
            tickets = ticketRepository.findByCreatedAtBetween(from, to);
        } else {
            tickets = ticketRepository.findAll();
        }

        List<TicketResponse> responses = new ArrayList<>();
        for (Ticket ticket : tickets) {
            responses.add(TicketResponse.fromEntity(ticket));
        }
        return responses;
    }

    public TicketResponse updateStatus(String ticketId, TicketStatusUpdateRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        Status oldStatus = ticket.getStatus();
        Status newStatus = request.getStatus();

        if (oldStatus == Status.SCADUTO) {
            throw new InvalidStatusException("Il ticket " + ticketId + " è SCADUTO e non può cambiare stato");
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        saveAudit(ticketId, oldStatus.name(), newStatus.name(), "UPDATE", "USER");

        return TicketResponse.fromEntity(saved);
    }

    public void deleteTicket(String ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        ticketRepository.delete(ticket);
        saveAudit(ticketId, ticket.getStatus().name(), null, "DELETE", "USER");
    }

    private Ticket findTicketOrThrow(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private String generateTicketId() {
        return "RF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePrivateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void saveAudit(String ticketId, String oldStatus, String newStatus, String operation, String source) {
        TicketAudit audit = new TicketAudit();
        audit.setTicketId(ticketId);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setOperation(operation);
        audit.setSource(source);
        audit.setChangedAt(LocalDateTime.now());
        ticketAuditRepository.save(audit);
    }
    public KpiResponse getKpi(LocalDateTime from, LocalDateTime to) {
        KpiResponse response = new KpiResponse();

        long venduti;
        if (from != null && to != null) {
            venduti = ticketRepository.countByStatusAndCreatedAtBetween(Status.VENDUTO, from, to);
        } else {
            venduti = ticketRepository.countByStatus(Status.VENDUTO);
        }
        response.setVenduti(venduti);

        Map<String, Long> distribuzione = new HashMap<>();
        for (Status status : Status.values()) {
            long count;
            if (from != null && to != null) {
                count = ticketRepository.countByStatusAndCreatedAtBetween(status, from, to);
            } else {
                count = ticketRepository.countByStatus(status);
            }
            distribuzione.put(status.name(), count);
        }
        response.setDistribuzionePerStato(distribuzione);

        return response;
    }
}