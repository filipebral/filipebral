package com.giro_app.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AuditExportSchedulerTest {
    @Mock private AuditEventRepository events;
    @Mock private AuditExportService exporter;
    @Mock private AuditAlertPort alerts;

    @Test
    void alertsOnExportFailuresAndContinuesWithNextEvent() {
        AuditEvent first = new AuditEvent(UUID.randomUUID(), "FIRST", null);
        AuditEvent second = new AuditEvent(UUID.randomUUID(), "SECOND", null);
        when(events.findDueForExport(any(Instant.class), any(Pageable.class))).thenReturn(List.of(first, second));
        when(exporter.export(first.getId())).thenThrow(new IllegalStateException("database unavailable"));
        when(exporter.export(second.getId())).thenReturn(false);

        new AuditExportScheduler(events, exporter, alerts).exportDue();

        verify(alerts).auditExportFailed(first.getId());
        verify(alerts).auditExportFailed(second.getId());
    }

    @Test
    void alertsWhenAuditBacklogCannotBeRead() {
        when(events.findDueForExport(any(Instant.class), any(Pageable.class)))
                .thenThrow(new IllegalStateException("database unavailable"));
        new AuditExportScheduler(events, exporter, alerts).exportDue();
        verify(alerts).auditBacklogUnavailable();
    }
}
