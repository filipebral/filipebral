package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "simulation_bills", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_bill_request", columnNames = {"user_id", "request_id"}))
public class SimulationBill {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "request_id", nullable = false) private UUID requestId;
    @Column(name = "execution_id", nullable = false) private UUID executionId;
    @Column(nullable = false, length = 100) private String description;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(nullable = false, length = 20) private String status;

    protected SimulationBill() {}

    public SimulationBill(UUID userId, UUID requestId, String description, BigDecimal amount, LocalDate dueDate) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestId = requestId;
        this.executionId = UUID.randomUUID();
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = "PENDING";
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getExecutionId() { return executionId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
