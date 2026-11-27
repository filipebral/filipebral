package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "in_app_notifications", uniqueConstraints = @UniqueConstraint(
        name = "uk_in_app_notification_event", columnNames = {"user_id", "event_key"}))
public class InAppNotification {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "event_key", nullable = false, length = 120) private String eventKey;
    @Column(nullable = false, length = 30) private String type;
    @Column(nullable = false, length = 160) private String message;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private boolean read;

    protected InAppNotification() {}

    public InAppNotification(UUID userId, String eventKey, String type, String message) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.eventKey = eventKey;
        this.type = type;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
    public void markRead() { read = true; }
}
