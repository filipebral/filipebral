package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface SimulationReconciliationRepository extends JpaRepository<SimulationReconciliation, UUID> {
    Optional<SimulationReconciliation> findByUserIdAndRequestId(UUID userId, UUID requestId);
    @Query("select r from SimulationReconciliation r where r.userId = :userId "
            + "order by case when r.status = 'UNKNOWN' then 0 else 1 end, r.createdAt desc")
    List<SimulationReconciliation> findForUser(@Param("userId") UUID userId, Pageable pageable);
    long countByUserIdAndStatus(UUID userId, String status);
    List<SimulationReconciliation> findTop100ByStatusAndNextCheckAtLessThanEqualOrderByNextCheckAtAsc(
            String status, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from SimulationReconciliation r where r.id = :id")
    Optional<SimulationReconciliation> findForUpdate(@Param("id") UUID id);
}
