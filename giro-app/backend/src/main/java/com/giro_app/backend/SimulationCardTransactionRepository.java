package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationCardTransactionRepository extends JpaRepository<SimulationCardTransaction, UUID> {
    Optional<SimulationCardTransaction> findByUserIdAndRequestId(UUID userId, UUID requestId);
    Optional<SimulationCardTransaction> findByUserIdAndId(UUID userId, UUID id);
    List<SimulationCardTransaction> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("select coalesce(sum(t.amount), 0) from SimulationCardTransaction t where t.userId = :userId and t.category = :category and t.kind = 'CREDIT' and t.createdAt >= :start and t.createdAt < :end")
    BigDecimal sumCreditPurchases(@Param("userId") UUID userId, @Param("category") String category,
                                  @Param("start") Instant start, @Param("end") Instant end);
}
