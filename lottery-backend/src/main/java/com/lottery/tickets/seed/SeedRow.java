package com.lottery.tickets.seed;

public class SeedRow {

    private final int rowNumber;
    private final String privateCode;
    private final String ticketId;
    private final String createdAt;
    private final String status;
    private final String extractAt;
    private final String updatedAt;

    public SeedRow(int rowNumber, String privateCode, String ticketId, String createdAt,
                    String status, String extractAt, String updatedAt) {
        this.rowNumber = rowNumber;
        this.privateCode = privateCode;
        this.ticketId = ticketId;
        this.createdAt = createdAt;
        this.status = status;
        this.extractAt = extractAt;
        this.updatedAt = updatedAt;
    }

    public int getRowNumber() { return rowNumber; }
    public String getPrivateCode() { return privateCode; }
    public String getTicketId() { return ticketId; }
    public String getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getExtractAt() { return extractAt; }
    public String getUpdatedAt() { return updatedAt; }
}