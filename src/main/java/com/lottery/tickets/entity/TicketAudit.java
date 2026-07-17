package com.lottery.tickets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_audit")
@Getter
@Setter
@NoArgsConstructor
public class TicketAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "operation", nullable = false, length = 30)
    private String operation;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}