package com.giro_app.backend;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.audit.export.enabled", havingValue = "true")
public class AuditExportScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditExportScheduler.class);
    private final AuditEventRepository events;
    private final AuditExportService exporter;
    private final AuditAlertPort alerts;

    AuditExportScheduler(AuditEventRepository events, AuditExportService exporter, AuditAlertPort alerts) {
        this.events = events;
        this.exporter = exporter;
        this.alerts = alerts;
    }

    @Scheduled(fixedDelayString = "${app.audit.export.delay-ms:30000}")
    public void exportDue() {
        List<AuditEvent> due;
        try {
            due = events.findDueForExport(Instant.now(), PageRequest.of(0, 100));
        } catch (RuntimeException exception) {
            LOGGER.error("Não foi possível consultar a fila de auditoria", exception);
            try {
                alerts.auditBacklogUnavailable();
            } catch (RuntimeException alertFailure) {
                LOGGER.error("Falha ao enviar alerta da fila de auditoria", alertFailure);
            }
            return;
        }
        for (AuditEvent event : due) {
            try {
                if (!exporter.export(event.getId())) alert(event);
            } catch (RuntimeException exception) {
                LOGGER.error("Falha na exportação de auditoria {}", event.getId(), exception);
                alert(event);
            }
        }
    }

    private void alert(AuditEvent event) {
        try {
            alerts.auditExportFailed(event.getId());
        } catch (RuntimeException exception) {
            LOGGER.error("Falha ao enviar alerta de auditoria {}", event.getId(), exception);
        }
    }
}
