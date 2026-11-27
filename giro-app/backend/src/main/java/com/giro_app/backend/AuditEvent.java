package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    private UUID id;
    @Column(name = "actor_user_id")
    private UUID actorUserId;
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    @Column(name = "resource_id")
    private UUID resourceId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "exported_at")
    private Instant exportedAt;
    @Column(name = "next_export_attempt_at")
    private Instant nextExportAttemptAt;

    protected AuditEvent() {}

    AuditEvent(UUID actorUserId, String eventType, UUID resourceId) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.resourceId = resourceId;
        this.createdAt = Instant.now();
    }

    public String getEventType() { return eventType; }
    public UUID getActorUserId() { return actorUserId; }
    public UUID getResourceId() { return resourceId; }
    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExportedAt() { return exportedAt; }
    Instant getNextExportAttemptAt() { return nextExportAttemptAt; }
    void markExported() { this.exportedAt = Instant.now(); this.nextExportAttemptAt = null; }
    void postponeExport() { this.nextExportAttemptAt = Instant.now().plusSeconds(300); }
}
