package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_goal_movements", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_goal_request", columnNames = {"user_id", "request_id"}))
public class SimulationGoalMovement {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "goal_id", nullable = false) private UUID goalId;
    @Column(name = "request_id", nullable = false) private UUID requestId;
    @Column(nullable = false, length = 10) private String type;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;

    protected SimulationGoalMovement() {}

    public SimulationGoalMovement(UUID userId, UUID goalId, UUID requestId, String type, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.goalId = goalId;
        this.requestId = requestId;
        this.type = type;
        this.amount = amount;
    }

    public UUID getGoalId() { return goalId; }
    public UUID getRequestId() { return requestId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
}
