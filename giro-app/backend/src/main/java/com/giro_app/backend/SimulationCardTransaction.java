package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simulation_card_transactions", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_card_request", columnNames = {"user_id", "request_id"}))
public class SimulationCardTransaction {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "request_id", nullable = false) private UUID requestId;
    @Column(nullable = false, length = 10) private String kind;
    @Column(nullable = false, length = 100) private String description;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "statement_month", length = 7) private String statementMonth;
    @Column(nullable = false) private Instant createdAt;
    @Column(length = 30) private String category;

    protected SimulationCardTransaction() {}

    public SimulationCardTransaction(UUID userId, UUID requestId, String kind, String description,
                                     BigDecimal amount, String statementMonth) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestId = requestId;
        this.kind = kind;
        this.description = description;
        this.amount = amount;
        this.statementMonth = statementMonth;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRequestId() { return requestId; }
    public String getKind() { return kind; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getStatementMonth() { return statementMonth; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
