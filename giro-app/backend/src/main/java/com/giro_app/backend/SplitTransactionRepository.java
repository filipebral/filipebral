package com.giro_app.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SplitTransactionRepository extends JpaRepository<SplitTransactionEntity, UUID> {
    List<SplitTransactionEntity> findBySplitGroupId(UUID splitGroupId);
}