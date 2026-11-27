package com.giro_app.backend;

import java.util.UUID;

/** External delivery contract; no email, push, or SMS adapter is configured in this application. */
public interface NotificationDeliveryPort {
    DeliveryResult deliver(Channel channel, UUID userId, String message, UUID idempotencyKey);

    enum Channel { EMAIL, PUSH, SMS }
    record DeliveryResult(String status, String providerReference) {}
}
