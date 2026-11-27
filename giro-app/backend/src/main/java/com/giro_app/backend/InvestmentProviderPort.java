package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.UUID;

/** Investment-provider boundary; calculator results are not offers or positions. */
public interface InvestmentProviderPort {
    InvestmentApplication apply(UUID customerReference, String productCode, BigDecimal amount, UUID idempotencyKey);

    record InvestmentApplication(String providerReference, String status) {}
}
