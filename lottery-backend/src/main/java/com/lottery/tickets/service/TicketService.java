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

    /**
     * Restituisce i ticket applicando i filtri opzionali status/from/to.
     * Gestisce tutte le combinazioni, incluso il caso in cui venga passato
     * solo uno dei due estremi dell'intervallo temporale (from senza to, o
     * viceversa): in tal caso il filtro è comunque applicato come "da from
     * in poi" o "fino a to", invece di essere ignorato.
     */
    public List<TicketResponse> getAllTickets(Status status, LocalDateTime from, LocalDateTime to) {
        List<Ticket> tickets;

        if (status != null && from != null && to != null) {
            tickets = ticketRepository.findByStatusAndCreatedAtBetween(status, from, to);
        } else if (status != null && from != null) {
            tickets = ticketRepository.findByStatusAndCreatedAtAfter(status, from);
        } else if (status != null && to != null) {
            tickets = ticketRepository.findByStatusAndCreatedAtBefore(status, to);
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (from != null && to != null) {
            tickets = ticketRepository.findByCreatedAtBetween(from, to);
        } else if (from != null) {
            tickets = ticketRepository.findByCreatedAtAfter(from);
        } else if (to != null) {
            tickets = ticketRepository.findByCreatedAtBefore(to);
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

    /**
     * Calcola i KPI applicando lo stesso criterio di filtro temporale di
     * getAllTickets: from e to possono essere passati singolarmente senza
     * che il filtro venga ignorato.
     */
    public KpiResponse getKpi(LocalDateTime from, LocalDateTime to) {
        KpiResponse response = new KpiResponse();

        response.setVenduti(countByStatusAndRange(Status.VENDUTO, from, to));

        Map<String, Long> distribuzione = new HashMap<>();
        for (Status status : Status.values()) {
            distribuzione.put(status.name(), countByStatusAndRange(status, from, to));
        }
        response.setDistribuzionePerStato(distribuzione);

        return response;
    }

    private long countByStatusAndRange(Status status, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return ticketRepository.countByStatusAndCreatedAtBetween(status, from, to);
        } else if (from != null) {
            return ticketRepository.countByStatusAndCreatedAtAfter(status, from);
        } else if (to != null) {
            return ticketRepository.countByStatusAndCreatedAtBefore(status, to);
        } else {
            return ticketRepository.countByStatus(status);
        }
    }
}
