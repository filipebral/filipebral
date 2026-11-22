package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationScheduleRepository extends JpaRepository<SimulationSchedule, UUID> {
    Optional<SimulationSchedule> findByUserIdAndRequestId(UUID userId, UUID requestId);
    List<SimulationSchedule> findTop50ByUserIdOrderByScheduledAtDesc(UUID userId);
    List<SimulationSchedule> findTop100ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(String status, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SimulationSchedule s where s.id = :id")
    Optional<SimulationSchedule> findForUpdate(@Param("id") UUID id);
}
