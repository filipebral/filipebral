package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_applications")
public class BusinessApplication {
    @Id
    private UUID id;

    @Column(name = "applicant_user_id", nullable = false, unique = true)
    private UUID applicantUserId;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(nullable = false, length = 255)
    private String cnpj;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "verification_requested_at")
    private Instant verificationRequestedAt;

    protected BusinessApplication() {}

    BusinessApplication(UUID applicantUserId, String cnpj) {
        this.id = UUID.randomUUID();
        this.applicantUserId = applicantUserId;
        this.cnpj = cnpj;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCnpj() { return cnpj; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getVerificationRequestedAt() { return verificationRequestedAt; }
    public void requestVerification() { this.verificationRequestedAt = Instant.now(); }
}
