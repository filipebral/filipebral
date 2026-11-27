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
@Table(name = "split_payment_shares", uniqueConstraints = @UniqueConstraint(
        name = "uk_split_payment_member", columnNames = {"split_transaction_id", "member_user_id"}))
public class SplitPaymentShare {
    @Id private UUID id;
    @Column(name = "split_transaction_id", nullable = false) private UUID splitTransactionId;
    @Column(name = "member_user_id", nullable = false) private UUID memberUserId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal allocatedAmount;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "paid_by") private UUID paidBy;

    protected SplitPaymentShare() {}

    public SplitPaymentShare(UUID splitTransactionId, UUID memberUserId, BigDecimal allocatedAmount) {
        this.id = UUID.randomUUID();
        this.splitTransactionId = splitTransactionId;
        this.memberUserId = memberUserId;
        this.allocatedAmount = allocatedAmount;
        this.status = "PENDING";
    }

    public UUID getId() { return id; }
    public UUID getSplitTransactionId() { return splitTransactionId; }
    public UUID getMemberUserId() { return memberUserId; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public String getStatus() { return status; }
    public Instant getPaidAt() { return paidAt; }
    public UUID getPaidBy() { return paidBy; }
    public void markPaid(UUID actor) {
        if ("PENDING".equals(status)) {
            status = "REPORTED_PAID";
            paidAt = Instant.now();
            paidBy = actor;
        }
    }
}
