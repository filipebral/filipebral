package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationBudgetRepository extends JpaRepository<SimulationBudget, UUID> {
    List<SimulationBudget> findByUserIdAndMonthOrderByCategory(UUID userId, String month);
    Optional<SimulationBudget> findByUserIdAndMonthAndCategory(UUID userId, String month, String category);
}
