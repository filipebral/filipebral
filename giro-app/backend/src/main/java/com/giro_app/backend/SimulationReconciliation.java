package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simulation_reconciliation")
public class SimulationReconciliation {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "request_id", nullable = false) private UUID requestId;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "transaction_id") private UUID transactionId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "next_check_at", nullable = false) private Instant nextCheckAt;

    protected SimulationReconciliation() {}

    SimulationReconciliation(UUID userId, UUID requestId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestId = requestId;
        this.status = "UNKNOWN";
        this.createdAt = Instant.now();
        this.nextCheckAt = this.createdAt;
    }

    void complete(UUID transactionId) {
        this.transactionId = transactionId;
        this.status = "COMPLETED";
    }

    void postpone(Instant nextCheckAt) { this.nextCheckAt = nextCheckAt; }
    void stop() { this.status = "STOPPED"; }
    void resume() { this.status = "UNKNOWN"; this.nextCheckAt = Instant.now(); }
    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    UUID getRequestId() { return requestId; }
    String getStatus() { return status; }
    UUID getTransactionId() { return transactionId; }
    Instant getCreatedAt() { return createdAt; }
}
