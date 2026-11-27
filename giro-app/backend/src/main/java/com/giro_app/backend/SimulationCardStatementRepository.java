package com.giro_app.backend;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationCardStatementRepository extends JpaRepository<SimulationCardStatement, UUID> {
    Optional<SimulationCardStatement> findByUserIdAndStatementMonth(UUID userId, String statementMonth);
    List<SimulationCardStatement> findTop24ByUserIdOrderByStatementMonthDesc(UUID userId);
    List<SimulationCardStatement> findByUserIdAndStatusOrderByStatementMonthDesc(UUID userId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SimulationCardStatement s where s.userId = :userId and s.statementMonth = :month")
    Optional<SimulationCardStatement> findForUpdate(@Param("userId") UUID userId, @Param("month") String month);
}
