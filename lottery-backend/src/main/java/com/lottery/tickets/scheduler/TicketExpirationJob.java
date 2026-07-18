package com.lottery.tickets.scheduler;

import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;
import com.lottery.tickets.entity.TicketAudit;
import com.lottery.tickets.repository.TicketAuditRepository;
import com.lottery.tickets.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class TicketExpirationJob {

    private final TicketRepository ticketRepository;
    private final TicketAuditRepository ticketAuditRepository;

    public TicketExpirationJob(TicketRepository ticketRepository, TicketAuditRepository ticketAuditRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketAuditRepository = ticketAuditRepository;
    }

    @Scheduled(cron = "0 * * * * *")
    public void expireTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Status> excludedStatuses = Arrays.asList(Status.SCADUTO, Status.VENDUTO);

        List<Ticket> ticketsToExpire = ticketRepository.findByExtractAtBeforeAndStatusNotIn(now, excludedStatuses);

        for (Ticket ticket : ticketsToExpire) {
            Status oldStatus = ticket.getStatus();
            ticket.setStatus(Status.SCADUTO);
            ticket.setUpdatedAt(now);
            ticketRepository.save(ticket);

            saveAudit(ticket.getTicketId(), oldStatus.name(), Status.SCADUTO.name());
        }
    }

    private void saveAudit(String ticketId, String oldStatus, String newStatus) {
        TicketAudit audit = new TicketAudit();
        audit.setTicketId(ticketId);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setOperation("AUTO_EXPIRE");
        audit.setSource("SCHEDULER");
        audit.setChangedAt(LocalDateTime.now());
        ticketAuditRepository.save(audit);
    }
}