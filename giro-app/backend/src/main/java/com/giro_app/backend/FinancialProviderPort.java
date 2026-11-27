package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provider boundary only. Implementations must not be enabled until provider, compliance, and settlement contracts are approved.
 */
public interface FinancialProviderPort {
    ProviderAccount openAccount(String customerReference, UUID idempotencyKey);
    ProviderPayment submitPayment(UUID accountReference, BigDecimal amount, String destination, UUID idempotencyKey);

    /**
     * Submission is not proof of settlement. An uncertain outcome must be resolved
     * using the same account and idempotency key before any retry.
     */
    PixPayment submitPix(PixRequest request);

    /**
     * An empty result means no operation was found; it does not prove that a
     * previous submission was not accepted. Provider errors must not return empty.
     */
    Optional<PixPayment> findPix(UUID accountReference, UUID idempotencyKey);

    record ProviderAccount(String providerReference, String status) {}
    record ProviderPayment(String providerReference, String status) {}

    enum PixKeyType { CPF, CNPJ, EMAIL, PHONE, RANDOM }
    enum PixStatus { PENDING, SETTLED, REJECTED, UNKNOWN }

    record PixRequest(UUID accountReference, UUID idempotencyKey, BigDecimal amount,
                      PixKeyType keyType, String key) {
        public PixRequest {
            Objects.requireNonNull(accountReference, "accountReference");
            Objects.requireNonNull(idempotencyKey, "idempotencyKey");
            Objects.requireNonNull(amount, "amount");
            Objects.requireNonNull(keyType, "keyType");
            if (amount.signum() <= 0 || amount.scale() > 2) {
                throw new IllegalArgumentException("Pix amount must be positive with at most two decimal places");
            }
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Pix key is required");
            }
        }

        @Override
        public String toString() {
            return "PixRequest[accountReference=" + accountReference + ", idempotencyKey=" + idempotencyKey
                    + ", amount=" + amount + ", keyType=" + keyType + ", key=REDACTED]";
        }
    }

    record PixPayment(String providerReference, PixStatus status) {
        public PixPayment {
            Objects.requireNonNull(status, "status");
        }
    }
}
