package com.giro_app.backend;

import java.util.UUID;

/**
 * Boundary for future business and representative verification; no provider is configured.
 * A matching CNPJ checksum does not establish company existence or representative authority.
 */
public interface BusinessVerificationProviderPort {
    Verification start(UUID applicationId, UUID idempotencyKey);
    Verification status(String providerReference);

    enum Status { PENDING, VERIFIED, REJECTED, UNAVAILABLE }

    /**
     * Results must be bound to the application and applicant. All checks require
     * separate authoritative evidence; a regular CNPJ alone is insufficient.
     * This contract does not grant business access.
     */
    record Verification(UUID applicationId, UUID applicantUserId, String providerReference,
                        Status companyRegistration, Status representativeIdentity,
                        Status representativeAuthority) {}
}
