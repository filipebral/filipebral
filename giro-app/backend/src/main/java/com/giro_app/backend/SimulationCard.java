package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_cards")
public class SimulationCard {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false)
    private boolean blocked;

    protected SimulationCard() {}

    public SimulationCard(UUID userId, BigDecimal creditLimit) {
        this.userId = userId;
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
