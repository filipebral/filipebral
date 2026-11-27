package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {
    List<InAppNotification> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<InAppNotification> findByUserIdAndEventKey(UUID userId, String eventKey);
    Optional<InAppNotification> findByUserIdAndId(UUID userId, UUID id);
}
