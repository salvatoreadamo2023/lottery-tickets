package com.lottery.tickets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seed_import_error")
@Getter
@Setter
@NoArgsConstructor
public class SeedImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "raw_ticket_id")
    private String rawTicketId;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;
}