package com.lottery.tickets.repository;

import com.lottery.tickets.entity.TicketAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAuditRepository extends JpaRepository<TicketAudit, Long> {
}