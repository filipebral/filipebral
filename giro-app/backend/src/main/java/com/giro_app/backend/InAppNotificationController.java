package com.giro_app.backend;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation/notifications")
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class InAppNotificationController {
    private final InAppNotificationService notifications;

    public InAppNotificationController(InAppNotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    public List<InAppNotificationService.NotificationView> list(@AuthenticationPrincipal Jwt principal) {
        return notifications.list(UUID.fromString(principal.getSubject()));
    }

    @PatchMapping("/{id}/read")
    public InAppNotificationService.NotificationView markRead(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        return notifications.markRead(UUID.fromString(principal.getSubject()), id);
    }

    @GetMapping("/channels")
    public List<InAppNotificationService.ChannelView> channels() { return notifications.channels(); }
}
