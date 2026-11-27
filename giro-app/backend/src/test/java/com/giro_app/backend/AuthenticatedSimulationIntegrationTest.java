package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "app.security.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.security.encryption.key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthenticatedSimulationIntegrationTest {
    @Container
    static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private AppUserRepository users;
    @Autowired private CpfLookupHashService cpfHashes;
    @Autowired private PasswordEncoder passwords;
    @Autowired private AuditEventRepository auditEvents;
    @Autowired private SimulationReconciliationService reconciliation;

    private UUID userId;

    @BeforeEach
    void seedUsers() {
        AppUser user = users.save(new AppUser("52998224725", cpfHashes.hash("52998224725"),
                "Cliente teste", passwords.encode("senha-teste-segura")));
        userId = user.getId();
        users.save(new AppUser("11144477735", cpfHashes.hash("11144477735"),
                "Outro teste", passwords.encode("senha-teste-segura")));
    }

    @Test
    void authenticatedOperationsCanBeRecoveredWithoutCrossUserAccess() throws Exception {
        mvc.perform(get("/simulation/account")).andExpect(status().isUnauthorized());
        mvc.perform(get("/simulation/history").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        String owner = login("52998224725");
        String other = login("11144477735");
        mvc.perform(post("/simulation/account").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
        mvc.perform(post("/simulation/account").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk());
        UUID request = UUID.randomUUID();
        String body = json.writeValueAsString(new SimulationController.Operation(request, new java.math.BigDecimal("10.00"), null));
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/simulation/credit").header("Authorization", "Bearer " + owner)
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(10.0));
        }
        String conflicting = json.writeValueAsString(new SimulationController.Operation(request, new java.math.BigDecimal("11.00"), null));
        mvc.perform(post("/simulation/credit").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(conflicting))
                .andExpect(status().isConflict());
        mvc.perform(get("/simulation/operations/{id}", request).header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("CREDIT"));
        mvc.perform(get("/simulation/operations/{id}", request).header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        mvc.perform(get("/simulation/history").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(get("/simulation/history").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));
        assertEquals(1, auditEvents.findAll().stream()
                .filter(event -> Objects.equals(event.getActorUserId(), userId)
                        && event.getEventType().equals("SIMULATION_OPERATION_CREDIT")).count());
    }

    @Test
    void businessApplicationStaysPendingAndPrivate() throws Exception {
        String owner = login("52998224725");
        String other = login("11144477735");
        mvc.perform(post("/api/business-applications").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"04252011000110\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.verification.companyRegistration").value("NOT_STARTED"))
                .andExpect(jsonPath("$.verification.representativeIdentity").value("NOT_STARTED"))
                .andExpect(jsonPath("$.verification.representativeAuthority").value("NOT_STARTED"));
        mvc.perform(get("/api/business-applications/current").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/business-applications/current").header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/business-applications/current/verification").header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/api/business-applications/current/verification").header("Authorization", "Bearer " + owner))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.verification.companyRegistration").value("WAITING_FOR_PROVIDER"))
                    .andExpect(jsonPath("$.verification.representativeIdentity").value("WAITING_FOR_PROVIDER"))
                    .andExpect(jsonPath("$.verification.representativeAuthority").value("WAITING_FOR_PROVIDER"));
        }
        mvc.perform(get("/api/business-applications/current").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verification.representativeAuthority").value("WAITING_FOR_PROVIDER"));
        assertEquals(1, auditEvents.findAll().stream()
                .filter(event -> Objects.equals(event.getActorUserId(), userId)
                        && event.getEventType().equals("BUSINESS_APPLICATION_CREATED")).count());
        assertEquals(1, auditEvents.findAll().stream()
                .filter(event -> Objects.equals(event.getActorUserId(), userId)
                        && event.getEventType().equals("BUSINESS_VERIFICATION_REQUESTED")).count());
    }

    @Test
    void loginAttemptsAreAuditedWithoutRecordingCredentials() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginRequestDTO("52998224725", "senha-incorreta"))))
                .andExpect(status().isUnauthorized());
        login("52998224725");
        var events = auditEvents.findAll();
        assertEquals(1, events.stream().filter(event -> event.getEventType().equals("LOGIN_REJECTED")
                && event.getActorUserId() == null && event.getResourceId() == null).count());
        assertEquals(1, events.stream().filter(event -> event.getEventType().equals("LOGIN_SUCCEEDED")
                && Objects.equals(event.getActorUserId(), userId) && event.getResourceId() == null).count());
    }

    @Test
    void reconciliationTracksOnlyCommittedSimulatedOperationsAndIsPrivate() throws Exception {
        String owner = login("52998224725");
        String other = login("11144477735");
        mvc.perform(post("/simulation/account").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
        UUID requestId = UUID.randomUUID();
        String register = "{\"requestId\":\"" + requestId + "\"}";
        String result = mvc.perform(post("/simulation/reconciliation").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNKNOWN"))
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(json.readTree(result).get("id").asText());
        mvc.perform(post("/simulation/reconciliation").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.toString()));
        reconciliation.check(id);
        mvc.perform(get("/simulation/reconciliation").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/simulation/reconciliation/{id}/stop", id).header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        mvc.perform(post("/simulation/credit").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                        new SimulationController.Operation(requestId, new java.math.BigDecimal("10.00"), null))))
                .andExpect(status().isOk());
        reconciliation.check(id);
        mvc.perform(get("/simulation/reconciliation").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("COMPLETED"));
        mvc.perform(get("/simulation/account").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(10.0));
    }

    @Test
    void auditExportKeepsFailuresPendingAndDoesNotResendAcknowledgedEvents() {
        AuditArchivePort archive = mock(AuditArchivePort.class);
        AuditExportService exporter = new AuditExportService(auditEvents, archive);
        AuditEvent success = auditEvents.save(new AuditEvent(userId, "TEST_SUCCESS", null));
        assertTrue(exporter.export(success.getId()));
        assertTrue(exporter.export(success.getId()));
        verify(archive).archive(any());
        AuditEvent failed = auditEvents.save(new AuditEvent(userId, "TEST_FAILURE", null));
        doThrow(new IllegalStateException("archive unavailable")).when(archive).archive(
                new AuditArchivePort.Record(failed.getId(), userId, "TEST_FAILURE", null, failed.getCreatedAt()));
        assertFalse(exporter.export(failed.getId()));
        assertEquals(null, failed.getExportedAt());
        assertTrue(failed.getNextExportAttemptAt().isAfter(java.time.Instant.now()));
    }

    @Test
    void cardAndGoalFlowsUseOnlyTheAuthenticatedUsersSimulatedBalance() throws Exception {
        String owner = login("52998224725");
        String other = login("11144477735");
        mvc.perform(post("/simulation/account").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
        mvc.perform(post("/simulation/credit").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new SimulationController.Operation(
                        UUID.randomUUID(), new java.math.BigDecimal("100.00"), null))))
                .andExpect(status().isOk());
        mvc.perform(post("/simulation/card").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content("{\"creditLimit\":50}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(true));
        String goalResponse = mvc.perform(post("/simulation/finance/goals").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Meta fictícia\",\"targetAmount\":50}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String goalId = json.readTree(goalResponse).get("id").asText();
        String movement = "{\"requestId\":\"" + UUID.randomUUID() + "\",\"amount\":30}";
        mvc.perform(post("/simulation/finance/goals/{id}/contributions", goalId)
                .header("Authorization", "Bearer " + other).contentType(MediaType.APPLICATION_JSON).content(movement))
                .andExpect(status().isNotFound());
        mvc.perform(post("/simulation/finance/goals/{id}/contributions", goalId)
                .header("Authorization", "Bearer " + owner).contentType(MediaType.APPLICATION_JSON).content(movement))
                .andExpect(status().isOk());
        mvc.perform(post("/simulation/finance/goals/{id}/contributions", goalId)
                .header("Authorization", "Bearer " + owner).contentType(MediaType.APPLICATION_JSON).content(movement))
                .andExpect(status().isOk());
        mvc.perform(get("/simulation/account").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.availableBalance").value(70.0));
        mvc.perform(get("/simulation/card").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(false));
        assertEquals(1, auditEvents.findAll().stream()
                .filter(event -> Objects.equals(event.getActorUserId(), userId)
                        && event.getEventType().equals("SIMULATION_GOAL_RESERVE")).count());
    }

    private String login(String cpf) throws Exception {
        String body = json.writeValueAsString(new LoginRequestDTO(cpf, "senha-teste-segura"));
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }
}
