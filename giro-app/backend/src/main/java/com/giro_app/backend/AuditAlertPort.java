package com.giro_app.backend;

import java.util.UUID;

/** Operational alert destination; implementations must not include credentials or personal data. */
public interface AuditAlertPort {
    void auditExportFailed(UUID eventId);
    void auditBacklogUnavailable();
}
