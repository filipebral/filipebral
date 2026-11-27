package com.giro_app.backend;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SplitPaymentShareRepository extends JpaRepository<SplitPaymentShare, UUID> {
    List<SplitPaymentShare> findBySplitTransactionIdOrderByMemberUserId(UUID splitTransactionId);
    Optional<SplitPaymentShare> findBySplitTransactionIdAndMemberUserId(UUID splitTransactionId, UUID memberUserId);
}
