package com.giro_app.backend;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    @Query("select a from AuditEvent a where a.exportedAt is null "
            + "and (a.nextExportAttemptAt is null or a.nextExportAttemptAt <= :now) order by a.createdAt, a.id")
    List<AuditEvent> findDueForExport(@Param("now") Instant now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AuditEvent a where a.id = :id")
    Optional<AuditEvent> findForUpdate(@Param("id") UUID id);
}
