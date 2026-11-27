package com.giro_app.backend;

import java.time.Instant;
import java.util.UUID;

/**
 * The archive must acknowledge only durable writes and deduplicate by eventId.
 * Immutability and retention must be enforced independently of the application.
 */
public interface AuditArchivePort {
    void archive(Record event);

    record Record(UUID eventId, UUID actorUserId, String eventType, UUID resourceId, Instant createdAt) {}
}
