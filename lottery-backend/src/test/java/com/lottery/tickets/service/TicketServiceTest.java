package com.lottery.tickets.service;

import com.lottery.tickets.dto.TicketStatusUpdateRequest;
import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;
import com.lottery.tickets.exception.InvalidStatusException;
import com.lottery.tickets.exception.TicketNotFoundException;
import com.lottery.tickets.repository.TicketAuditRepository;
import com.lottery.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketAuditRepository ticketAuditRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket scadutoTicket;

    @BeforeEach
    void setUp() {
        scadutoTicket = new Ticket();
        scadutoTicket.setId(1L);
        scadutoTicket.setTicketId("LT-TEST0001");
        scadutoTicket.setPrivateCode("PRIVATECODE0001");
        scadutoTicket.setStatus(Status.SCADUTO);
        scadutoTicket.setCreatedAt(LocalDateTime.now().minusDays(10));
        scadutoTicket.setExtractAt(LocalDateTime.now().minusDays(1));
        scadutoTicket.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void updateStatus_shouldThrowException_whenTicketIsScaduto() {
        when(ticketRepository.findByTicketId("LT-TEST0001")).thenReturn(Optional.of(scadutoTicket));

        TicketStatusUpdateRequest request = new TicketStatusUpdateRequest();
        request.setStatus(Status.VENDUTO);

        assertThrows(InvalidStatusException.class, () ->
                ticketService.updateStatus("LT-TEST0001", request));
    }

    @Test
    void getTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findByTicketId("LT-NOTEXIST")).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () ->
                ticketService.getTicket("LT-NOTEXIST"));
    }
}