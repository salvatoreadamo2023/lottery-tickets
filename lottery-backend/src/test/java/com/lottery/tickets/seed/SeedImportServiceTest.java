package com.lottery.tickets.seed;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedImportServiceTest {

    private final SeedImportService seedImportService = new SeedImportService(null, null, null);

    // caso base: una riga senza problemi non deve generare errori
    @Test
    void validateRowCompleto_shouldReturnNoErrors_whenRowIsClean() {
        SeedRow row = new SeedRow(1, "PRIVATE001", "TICKET001",
                "2024-01-01T10:00:00Z", "CREATO", "2024-12-01T10:00:00Z", "2024-01-02T10:00:00Z");

        Map<String, Integer> ticketIdOccurrences = new HashMap<>();
        ticketIdOccurrences.put("TICKET001", 1);
        Map<String, Integer> privateCodeOccurrences = new HashMap<>();
        privateCodeOccurrences.put("PRIVATE001", 1);

        List<String> errors = seedImportService.validateRowCompleto(row, ticketIdOccurrences, privateCodeOccurrences);

        assertTrue(errors.isEmpty());
    }

    // caso sporco: extract_at prima di created_at deve essere scartato
    @Test
    void validateRowCompleto_shouldReturnError_whenExtractAtIsBeforeCreatedAt() {
        SeedRow row = new SeedRow(3, "PRIVATE003", "TICKET003",
                "2024-06-01T10:00:00Z", "CREATO", "2024-01-01T10:00:00Z", "2024-06-02T10:00:00Z");

        Map<String, Integer> ticketIdOccurrences = new HashMap<>();
        ticketIdOccurrences.put("TICKET003", 1);
        Map<String, Integer> privateCodeOccurrences = new HashMap<>();
        privateCodeOccurrences.put("PRIVATE003", 1);

        List<String> errors = seedImportService.validateRowCompleto(row, ticketIdOccurrences, privateCodeOccurrences);

        assertTrue(errors.contains("extract_at precedente a created_at"));
    }
}