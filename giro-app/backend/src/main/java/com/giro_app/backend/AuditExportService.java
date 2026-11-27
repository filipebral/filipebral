package com.giro_app.backend;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.audit.export.enabled", havingValue = "true")
public class AuditExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditExportService.class);
    private final AuditEventRepository events;
    private final AuditArchivePort archive;

    AuditExportService(AuditEventRepository events, AuditArchivePort archive) {
        this.events = events;
        this.archive = archive;
    }

    @Transactional
    public boolean export(UUID eventId) {
        AuditEvent event = events.findForUpdate(eventId).orElseThrow();
        if (event.getExportedAt() != null) return true;
        if (event.getNextExportAttemptAt() != null && event.getNextExportAttemptAt().isAfter(Instant.now())) return true;
        try {
            archive.archive(new AuditArchivePort.Record(event.getId(), event.getActorUserId(),
                    event.getEventType(), event.getResourceId(), event.getCreatedAt()));
        } catch (RuntimeException exception) {
            event.postponeExport();
            LOGGER.error("Falha ao exportar evento de auditoria {}", eventId, exception);
            return false;
        }
        event.markExported();
        return true;
    }
}
