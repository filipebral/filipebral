package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationTransactionRepository extends JpaRepository<SimulationTransaction, UUID> {
    Optional<SimulationTransaction> findByUserIdAndRequestId(UUID userId, UUID requestId);
    List<SimulationTransaction> findTop50ByUserIdOrderByCreatedAtDescIdDesc(UUID userId);
    Slice<SimulationTransaction> findByUserIdOrderByCreatedAtDescIdDesc(UUID userId, Pageable pageable);
    Optional<SimulationTransaction> findByUserIdAndId(UUID userId, UUID id);

    @Query("select coalesce(sum(t.amount), 0) from SimulationTransaction t where t.userId = :userId and t.category = :category and t.createdAt >= :start and t.createdAt < :end and t.type <> 'CREDIT' and t.type <> 'CARD_BILL'")
    BigDecimal sumExpenses(@Param("userId") UUID userId, @Param("category") String category,
                           @Param("start") Instant start, @Param("end") Instant end);
}
