package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SplitTransactionResponseDTO(
    UUID transactionId,
    String description,
    BigDecimal totalAmount,
    List<MemberShareDTO> shares
) {
    public record MemberShareDTO(
        UUID userId,
        BigDecimal percentage,
        BigDecimal allocatedAmount
    ) {}
}