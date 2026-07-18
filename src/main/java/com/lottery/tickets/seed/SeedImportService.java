package com.lottery.tickets.seed;

import com.lottery.tickets.entity.SeedImportError;
import com.lottery.tickets.entity.Status;
import com.lottery.tickets.entity.Ticket;
import com.lottery.tickets.entity.TicketAudit;
import com.lottery.tickets.repository.SeedImportErrorRepository;
import com.lottery.tickets.repository.TicketAuditRepository;
import com.lottery.tickets.repository.TicketRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SeedImportService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedImportService.class);
    private static final String SEED_FILE_PATH = "seed/ticket_seed.xlsx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final TicketRepository ticketRepository;
    private final TicketAuditRepository ticketAuditRepository;
    private final SeedImportErrorRepository seedImportErrorRepository;

    public SeedImportService(TicketRepository ticketRepository,
                              TicketAuditRepository ticketAuditRepository,
                              SeedImportErrorRepository seedImportErrorRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketAuditRepository = ticketAuditRepository;
        this.seedImportErrorRepository = seedImportErrorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (ticketRepository.count() > 0) {
            log.info("Seed import saltato: la tabella ticket contiene già dati");
            return;
        }
        importSeedFile();
    }

    private void importSeedFile() throws Exception {
        List<SeedRow> rows = readRows();

        Map<String, Integer> ticketIdOccurrences = countTicketIdOccurrences(rows);
        Map<String, Integer> privateCodeOccurrences = countPrivateCodeOccurrences(rows);

        int imported = 0;
        Map<String, Integer> discardReasons = new HashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            SeedRow row = rows.get(i);
            List<String> errors = validateRowCompleto(row, ticketIdOccurrences, privateCodeOccurrences);

            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < errors.size(); j++) {
                    sb.append(errors.get(j));
                    if (j < errors.size() - 1) {
                        sb.append(", ");
                    }
                }
                String finalReason = sb.toString();
                
                log.warn("Riga {} scartata: {}", row.getRowNumber(), finalReason);

                Integer currentCount = discardReasons.get(finalReason);
                if (currentCount == null) {
                    discardReasons.put(finalReason, 1);
                } else {
                    discardReasons.put(finalReason, currentCount + 1);
                }

                saveImportError(row, finalReason);
                continue;
            }
            Ticket ticket = buildTicket(row);
            ticketRepository.save(ticket);
            saveAudit(ticket.getTicketId(), ticket.getStatus().name());
            imported++;
        }

        log.info("Seed import completato: {} righe totali, {} importate, {} scartate",
                rows.size(), imported, rows.size() - imported);
        for (Map.Entry<String, Integer> entry : discardReasons.entrySet()) {
            log.info("  - {}: {} righe", entry.getKey(), entry.getValue());
        }
    }

    private List<SeedRow> readRows() throws Exception {
        List<SeedRow> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);

        try (InputStream is = resource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                SeedRow seedRow = new SeedRow(
                        i + 1,
                        getCellValue(row.getCell(0)),
                        getCellValue(row.getCell(1)),
                        getCellValue(row.getCell(2)),
                        getCellValue(row.getCell(3)),
                        getCellValue(row.getCell(4)),
                        getCellValue(row.getCell(5))
                );
                rows.add(seedRow);
            }
        }
        return rows;
    }

    private String getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        String value = cell.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private Map<String, Integer> countTicketIdOccurrences(List<SeedRow> rows) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String value = rows.get(i).getTicketId();
            if (value != null) {
                Integer currentCount = counts.get(value);
                if (currentCount == null) {
                    counts.put(value, 1);
                } else {
                    counts.put(value, currentCount + 1);
                }
            }
        }
        return counts;
    }

    private Map<String, Integer> countPrivateCodeOccurrences(List<SeedRow> rows) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String value = rows.get(i).getPrivateCode();
            if (value != null) {
                Integer currentCount = counts.get(value);
                if (currentCount == null) {
                    counts.put(value, 1);
                } else {
                    counts.put(value, currentCount + 1);
                }
            }
        }
        return counts;
    }

    private List<String> validateRowCompleto(SeedRow row, Map<String, Integer> ticketIdOccurrences, Map<String, Integer> privateCodeOccurrences) {
        List<String> errors = new ArrayList<>();

        if (row.getTicketId() == null) {
            errors.add("ticket_id mancante");
        } else {
            Integer count = ticketIdOccurrences.get(row.getTicketId());
            if (count != null && count > 1) {
                errors.add("ticket_id duplicato");
            }
        }

        if (row.getPrivateCode() == null) {
            errors.add("private_code mancante");
        } else {
            Integer count = privateCodeOccurrences.get(row.getPrivateCode());
            if (count != null && count > 1) {
                errors.add("private_code duplicato");
            }
        }

        if (row.getStatus() == null || !isValidStatus(row.getStatus())) {
            errors.add("status mancante o non valido");
        }

        if (row.getExtractAt() == null) {
            errors.add("extract_at mancante");
        }

        LocalDateTime createdAt = parseDate(row.getCreatedAt());
        LocalDateTime extractAt = parseDate(row.getExtractAt());
        LocalDateTime updatedAt = parseDate(row.getUpdatedAt());

        if (row.getCreatedAt() != null && createdAt == null) {
            errors.add("created_at non valido o non parsabile");
        }
        if (row.getExtractAt() != null && extractAt == null) {
            errors.add("extract_at non valido o non parsabile");
        }
        if (row.getUpdatedAt() != null && updatedAt == null) {
            errors.add("updated_at non valido o non parsabile");
        }

        if (createdAt != null && extractAt != null && extractAt.isBefore(createdAt)) {
            errors.add("extract_at precedente a created_at");
        }
        if (createdAt != null && updatedAt != null && updatedAt.isBefore(createdAt)) {
            errors.add("updated_at precedente a created_at");
        }

        return errors;
    }

    private boolean isValidStatus(String status) {
        String normalized = status.trim().toUpperCase();
        Status[] statuses = Status.values();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].name().equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private LocalDateTime parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            OffsetDateTime parsed = OffsetDateTime.parse(value, DATE_FORMAT);
            return parsed.toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private Ticket buildTicket(SeedRow row) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(row.getTicketId());
        ticket.setPrivateCode(row.getPrivateCode());
        ticket.setStatus(Status.valueOf(row.getStatus().trim().toUpperCase()));
        ticket.setCreatedAt(parseDate(row.getCreatedAt()));
        ticket.setExtractAt(parseDate(row.getExtractAt()));
        ticket.setUpdatedAt(parseDate(row.getUpdatedAt()));
        return ticket;
    }

    private void saveAudit(String ticketId, String newStatus) {
        TicketAudit audit = new TicketAudit();
        audit.setTicketId(ticketId);
        audit.setOldStatus(null);
        audit.setNewStatus(newStatus);
        audit.setOperation("SEED_IMPORT");
        audit.setSource("SEED");
        audit.setChangedAt(LocalDateTime.now());
        ticketAuditRepository.save(audit);
    }

    private void saveImportError(SeedRow row, String reason) {
        SeedImportError error = new SeedImportError();
        error.setRowNumber(row.getRowNumber());
        error.setRawTicketId(row.getTicketId());
        error.setReason(reason);
        error.setImportedAt(LocalDateTime.now());
        seedImportErrorRepository.save(error);
    }
}