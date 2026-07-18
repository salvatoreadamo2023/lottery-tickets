package com.lottery.tickets.repository;

import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketId(String ticketId);

    boolean existsByTicketId(String ticketId);

    boolean existsByPrivateCode(String privateCode);

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Ticket> findByStatusAndCreatedAtBetween(Status status, LocalDateTime from, LocalDateTime to);

    List<Ticket> findByExtractAtBeforeAndStatusNotIn(LocalDateTime now, List<Status> excludedStatuses);
    
    long countByStatus(Status status);

    long countByStatusAndCreatedAtBetween(Status status, LocalDateTime from, LocalDateTime to);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}