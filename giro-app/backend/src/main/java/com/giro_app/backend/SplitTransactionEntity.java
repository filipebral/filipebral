package com.giro_app.backend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "split_transactions")
@Getter
@Setter
@NoArgsConstructor
public class SplitTransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID splitGroupId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SplitTransactionEntity(UUID id, UUID splitGroupId, String description, BigDecimal totalAmount) {
        this.id = id;
        this.splitGroupId = splitGroupId;
        this.description = description;
        this.totalAmount = totalAmount;
    }
}
