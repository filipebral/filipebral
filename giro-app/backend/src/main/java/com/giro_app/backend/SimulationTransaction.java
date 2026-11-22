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
@Table(name = "simulation_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_simulation_request", columnNames = {"user_id", "request_id"}))
public class SimulationTransaction {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SimulationTransaction() {}

    public SimulationTransaction(UUID userId, UUID requestId, String type, BigDecimal amount, String description) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestId = requestId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
