package com.giro_app.backend;

import java.util.UUID;

/** Card-provider boundary. The application currently exposes simulation only. */
public interface CardProviderPort {
    CardOperation issue(UUID customerReference, String cardKind, UUID idempotencyKey);
    CardOperation setBlocked(String providerCardReference, boolean blocked, UUID idempotencyKey);
    CardOperation setLimit(String providerCardReference, java.math.BigDecimal limit, UUID idempotencyKey);

    record CardOperation(String providerReference, String status) {}
}
