package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.UUID;

/** Credit-provider boundary; simulator estimates are never submitted to this port. */
public interface CreditProviderPort {
    CreditApplication submit(UUID customerReference, BigDecimal amount, int termMonths, UUID idempotencyKey);

    record CreditApplication(String providerReference, String status, String declineReasonCode) {}
}
