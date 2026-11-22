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
@Table(name = "simulation_schedules", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_schedule_request", columnNames = {"user_id", "request_id"}))
public class SimulationSchedule {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "request_id", nullable = false)
    private UUID requestId;
    @Column(name = "execution_id", nullable = false)
    private UUID executionId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 100)
    private String destination;
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;
    @Column(nullable = false, length = 20)
    private String status;

    protected SimulationSchedule() {}

    public SimulationSchedule(UUID userId, UUID requestId, BigDecimal amount, String destination, Instant scheduledAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestId = requestId;
        this.executionId = UUID.randomUUID();
        this.amount = amount;
        this.destination = destination;
        this.scheduledAt = scheduledAt;
        this.status = "PENDING";
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getRequestId() { return requestId; }
    public UUID getExecutionId() { return executionId; }
    public BigDecimal getAmount() { return amount; }
    public String getDestination() { return destination; }
    public Instant getScheduledAt() { return scheduledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
