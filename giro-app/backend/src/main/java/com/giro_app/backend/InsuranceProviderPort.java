package com.giro_app.backend;

import java.util.UUID;

/** Insurance-provider boundary; no policy or coverage is represented by the simulation. */
public interface InsuranceProviderPort {
    InsuranceApplication apply(UUID customerReference, String productCode, UUID idempotencyKey);

    record InsuranceApplication(String providerReference, String status) {}
}
