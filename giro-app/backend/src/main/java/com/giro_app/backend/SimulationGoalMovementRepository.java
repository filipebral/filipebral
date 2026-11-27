package com.giro_app.backend;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationGoalMovementRepository extends JpaRepository<SimulationGoalMovement, UUID> {
    Optional<SimulationGoalMovement> findByUserIdAndRequestId(UUID userId, UUID requestId);
}
