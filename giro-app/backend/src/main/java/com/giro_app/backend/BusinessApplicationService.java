package com.giro_app.backend;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessApplicationService {
    private final AppUserRepository users;
    private final BusinessApplicationRepository applications;
    private final AuditEventRepository auditEvents;

    public BusinessApplicationService(AppUserRepository users, BusinessApplicationRepository applications,
            AuditEventRepository auditEvents) {
        this.users = users;
        this.applications = applications;
        this.auditEvents = auditEvents;
    }

    @Transactional
    public ApplicationView create(UUID userId, String cnpj) {
        if (!CnpjValidator.isValid(cnpj)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um CNPJ válido.");
        }
        users.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        if (applications.findByApplicantUserId(userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma solicitação comercial para este usuário.");
        }
        String normalized = cnpj.toUpperCase(Locale.ROOT).replace(".", "").replace("/", "").replace("-", "");
        BusinessApplication application = applications.save(new BusinessApplication(userId, normalized));
        auditEvents.save(new AuditEvent(userId, "BUSINESS_APPLICATION_CREATED", application.getId()));
        return view(application);
    }

    @Transactional(readOnly = true)
    public ApplicationView current(UUID userId) {
        return applications.findByApplicantUserId(userId).map(this::view)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação comercial não encontrada."));
    }

    @Transactional
    public ApplicationView requestVerification(UUID userId) {
        BusinessApplication application = applications.findForUpdateByApplicantUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação comercial não encontrada."));
        if (application.getVerificationRequestedAt() == null) {
            application.requestVerification();
            auditEvents.save(new AuditEvent(userId, "BUSINESS_VERIFICATION_REQUESTED", application.getId()));
        }
        return view(application);
    }

    private ApplicationView view(BusinessApplication application) {
        String checkStatus = application.getVerificationRequestedAt() == null ? "NOT_STARTED" : "WAITING_FOR_PROVIDER";
        return new ApplicationView(application.getId(), application.getCnpj(), "PENDING", application.getCreatedAt(),
                new VerificationProgress(checkStatus, checkStatus, checkStatus));
    }

    public record ApplicationView(UUID id, String cnpj, String status, Instant createdAt,
                                  VerificationProgress verification) {}
    public record VerificationProgress(String companyRegistration, String representativeIdentity,
                                       String representativeAuthority) {}
}
