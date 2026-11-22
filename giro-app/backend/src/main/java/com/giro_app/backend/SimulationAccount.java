package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_accounts")
public class SimulationAccount {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected SimulationAccount() {}

    public SimulationAccount(UUID userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
