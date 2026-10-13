package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitTransactionRequestDTO(
    UUID splitGroupId,
    BigDecimal totalAmount,
    String description
) {}