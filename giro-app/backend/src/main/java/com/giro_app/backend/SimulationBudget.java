package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_budgets", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_budget", columnNames = {"user_id", "budget_month", "category"}))
public class SimulationBudget {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "budget_month", nullable = false, length = 7) private String month;
    @Column(nullable = false, length = 30) private String category;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal limitAmount;

    protected SimulationBudget() {}

    public SimulationBudget(UUID userId, String month, String category, BigDecimal limitAmount) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.month = month;
        this.category = category;
        this.limitAmount = limitAmount;
    }

    public UUID getId() { return id; }
    public String getMonth() { return month; }
    public String getCategory() { return category; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
}
