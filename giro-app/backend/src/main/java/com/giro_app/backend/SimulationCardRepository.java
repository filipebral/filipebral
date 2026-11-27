package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationCardRepository extends JpaRepository<SimulationCard, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SimulationCard c where c.userId = :userId")
    Optional<SimulationCard> findForUpdate(@Param("userId") UUID userId);
}
