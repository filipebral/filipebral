package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationGoalRepository extends JpaRepository<SimulationGoal, UUID> {
    List<SimulationGoal> findByUserIdOrderByName(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from SimulationGoal g where g.id = :id and g.userId = :userId")
    Optional<SimulationGoal> findForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
}
