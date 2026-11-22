package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationTransactionRepository extends JpaRepository<SimulationTransaction, UUID> {
    Optional<SimulationTransaction> findByUserIdAndRequestId(UUID userId, UUID requestId);
    List<SimulationTransaction> findTop50ByUserIdOrderByCreatedAtDescIdDesc(UUID userId);
}
