package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_goals")
public class SimulationGoal {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false, length = 80) private String name;
    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2) private BigDecimal targetAmount;
    @Column(name = "saved_amount", nullable = false, precision = 19, scale = 2) private BigDecimal savedAmount;

    protected SimulationGoal() {}

    public SimulationGoal(UUID userId, String name, BigDecimal targetAmount) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = BigDecimal.ZERO.setScale(2);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public void add(BigDecimal amount) { savedAmount = savedAmount.add(amount); }
    public void release(BigDecimal amount) { savedAmount = savedAmount.subtract(amount); }
}
