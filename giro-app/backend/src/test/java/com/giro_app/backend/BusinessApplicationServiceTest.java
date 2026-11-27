package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BusinessApplicationServiceTest {
    @Mock private AppUserRepository users;
    @Mock private BusinessApplicationRepository applications;
    @Mock private AuditEventRepository auditEvents;

    @Test
    void validCnpjCreatesOnlyPendingApplication() {
        UUID userId = UUID.randomUUID();
        when(users.findForUpdate(userId)).thenReturn(Optional.of(new AppUser("52998224725", "hash", "Cliente", "hash")));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = new BusinessApplicationService(users, applications, auditEvents).create(userId, "04.252.011/0001-10");
        assertEquals("04252011000110", result.cnpj());
        assertEquals("PENDING", result.status());
        verify(auditEvents).save(org.mockito.ArgumentMatchers.argThat(event ->
                event.getActorUserId().equals(userId) && event.getResourceId().equals(result.id())
                        && event.getEventType().equals("BUSINESS_APPLICATION_CREATED")));
    }

    @Test
    void invalidCnpjCannotBeSaved() {
        var service = new BusinessApplicationService(users, applications, auditEvents);
        var error = assertThrows(ResponseStatusException.class, () -> service.create(UUID.randomUUID(), "04252011000111"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(applications, never()).save(any());
    }

    @Test
    void acceptsAndNormalizesAlphanumericCnpj() {
        UUID userId = UUID.randomUUID();
        when(users.findForUpdate(userId)).thenReturn(Optional.of(new AppUser("52998224725", "hash", "Cliente", "hash")));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = new BusinessApplicationService(users, applications, auditEvents).create(userId, "12.abc.345/01de-35");
        assertEquals("12ABC34501DE35", result.cnpj());
        assertEquals("PENDING", result.status());
    }

    @Test
    void nonexistentApplicantCannotCreateApplication() {
        var service = new BusinessApplicationService(users, applications, auditEvents);
        var error = assertThrows(ResponseStatusException.class,
                () -> service.create(UUID.randomUUID(), "04252011000110"));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verify(applications, never()).save(any());
    }

    @Test
    void duplicateApplicationIsRejected() {
        UUID userId = UUID.randomUUID();
        when(users.findForUpdate(userId)).thenReturn(Optional.of(new AppUser("52998224725", "hash", "Cliente", "hash")));
        when(applications.findByApplicantUserId(userId))
                .thenReturn(Optional.of(new BusinessApplication(userId, "04252011000110")));
        var error = assertThrows(ResponseStatusException.class,
                () -> new BusinessApplicationService(users, applications, auditEvents).create(userId, "04252011000110"));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(applications, never()).save(any());
    }

    @Test
    void currentApplicationIsScopedToApplicant() {
        UUID userId = UUID.randomUUID();
        when(applications.findByApplicantUserId(userId))
                .thenReturn(Optional.of(new BusinessApplication(userId, "04252011000110")));
        assertEquals("PENDING", new BusinessApplicationService(users, applications, auditEvents).current(userId).status());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> new BusinessApplicationService(users, applications, auditEvents).current(UUID.randomUUID())).getStatusCode());
    }
}
