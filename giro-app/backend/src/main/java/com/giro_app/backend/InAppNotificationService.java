package com.giro_app.backend;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class InAppNotificationService {
    private final InAppNotificationRepository repository;

    public InAppNotificationService(InAppNotificationRepository repository) { this.repository = repository; }

    @Transactional
    public InAppNotification createOnce(UUID userId, String key, String type, String message) {
        return repository.findByUserIdAndEventKey(userId, key)
                .orElseGet(() -> repository.save(new InAppNotification(userId, key, type, message)));
    }

    @Transactional(readOnly = true)
    public List<NotificationView> list(UUID userId) {
        return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationView::from).toList();
    }

    @Transactional
    public NotificationView markRead(UUID userId, UUID id) {
        InAppNotification notification = repository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada."));
        notification.markRead();
        return NotificationView.from(notification);
    }

    public List<ChannelView> channels() {
        return List.of(new ChannelView("EMAIL", "NOT_CONFIGURED"), new ChannelView("PUSH", "NOT_CONFIGURED"),
                new ChannelView("SMS", "NOT_CONFIGURED"));
    }

    public record NotificationView(UUID id, String type, String message, Instant createdAt, boolean read) {
        static NotificationView from(InAppNotification notification) {
            return new NotificationView(notification.getId(), notification.getType(), notification.getMessage(),
                    notification.getCreatedAt(), notification.isRead());
        }
    }
    public record ChannelView(String channel, String status) {}
}
