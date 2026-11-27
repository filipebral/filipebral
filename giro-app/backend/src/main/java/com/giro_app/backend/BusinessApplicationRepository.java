package com.giro_app.backend;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface BusinessApplicationRepository extends JpaRepository<BusinessApplication, UUID> {
    Optional<BusinessApplication> findByApplicantUserId(UUID applicantUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BusinessApplication a where a.applicantUserId = :userId")
    Optional<BusinessApplication> findForUpdateByApplicantUserId(@Param("userId") UUID userId);
}
