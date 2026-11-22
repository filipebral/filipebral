package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationBillRepository extends JpaRepository<SimulationBill, UUID> {
    Optional<SimulationBill> findByUserIdAndRequestId(UUID userId, UUID requestId);
    List<SimulationBill> findTop50ByUserIdOrderByDueDateDescIdDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from SimulationBill b where b.id = :id")
    Optional<SimulationBill> findForUpdate(@Param("id") UUID id);
}
