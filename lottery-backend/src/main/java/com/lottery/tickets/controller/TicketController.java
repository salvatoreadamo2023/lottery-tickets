package com.lottery.tickets.controller;

import com.lottery.tickets.dto.KpiResponse;
import com.lottery.tickets.dto.TicketRequest;
import com.lottery.tickets.dto.TicketResponse;
import com.lottery.tickets.dto.TicketStatusUpdateRequest;
import com.lottery.tickets.entity.Status;
import com.lottery.tickets.service.TicketService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

//creazione ticket
	@PostMapping
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request) {
		TicketResponse response = ticketService.createTicket(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * @GetMapping public ResponseEntity<List<TicketResponse>> getAllTickets() {
	 * return ResponseEntity.ok(ticketService.getAllTickets()); }
	 */
	// lista ticket con filtro date
	@GetMapping
	public ResponseEntity<List<TicketResponse>> getAllTickets(@RequestParam(required = false) Status status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return ResponseEntity.ok(ticketService.getAllTickets(status, from, to));
	}

	@GetMapping("/{ticketId}")
	public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketId) {
		return ResponseEntity.ok(ticketService.getTicket(ticketId));
	}

	@PutMapping("/{ticketId}/status")
	public ResponseEntity<TicketResponse> updateStatus(@PathVariable String ticketId,
			@Valid @RequestBody TicketStatusUpdateRequest request) {
		return ResponseEntity.ok(ticketService.updateStatus(ticketId, request));
	}

	@DeleteMapping("/{ticketId}")
	public ResponseEntity<Void> deleteTicket(@PathVariable String ticketId) {
		ticketService.deleteTicket(ticketId);
		return ResponseEntity.noContent().build();
	}

//lista kpi aggregati con filtro date
	@GetMapping("/kpi")
	public ResponseEntity<KpiResponse> getKpi(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return ResponseEntity.ok(ticketService.getKpi(from, to));
	}
}