package com.giro_app.backend;

import java.math.BigDecimal;

public record BaaSAccountResponse(
    String baasAccountId,
    String accountNumber,
    String branch,
    String routingNumber,
    BigDecimal initialBalance,
    String status
) {}