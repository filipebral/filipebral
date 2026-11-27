package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_card_statements", uniqueConstraints = @UniqueConstraint(
        name = "uk_simulation_card_statement", columnNames = {"user_id", "statement_month"}))
public class SimulationCardStatement {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "statement_month", nullable = false, length = 7) private String statementMonth;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "payment_request_id") private UUID paymentRequestId;

    protected SimulationCardStatement() {}

    public SimulationCardStatement(UUID userId, String statementMonth) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.statementMonth = statementMonth;
        this.amount = BigDecimal.ZERO.setScale(2);
        this.status = "OPEN";
    }

    public UUID getId() { return id; }
    public String getStatementMonth() { return statementMonth; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public UUID getPaymentRequestId() { return paymentRequestId; }
    public void add(BigDecimal value) { amount = amount.add(value); }
    public void pay(UUID requestId) { status = "PAID"; paymentRequestId = requestId; }
}
