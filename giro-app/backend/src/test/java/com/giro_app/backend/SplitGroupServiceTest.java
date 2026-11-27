package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
@Import({ProcessSplitTransactionUseCase.class, SplitGroupService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SplitGroupServiceTest {
    @Autowired private SplitGroupRepository groups;
    @Autowired private ProcessSplitTransactionUseCase process;
    @Autowired private SplitGroupService service;

    @Test
    void ownerAndParticipantCanOnlyRecordManualPaymentForTheirAllowedShares() {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SplitGroup group = groups.saveAndFlush(new SplitGroup(owner, "Casa", List.of(
                new SplitGroup.GroupMemberInput(owner, new BigDecimal("50.00")),
                new SplitGroup.GroupMemberInput(member, new BigDecimal("50.00")))));
        var result = process.execute(new SplitTransactionRequestDTO(group.getId(), new BigDecimal("80.00"), "Compra fictícia"), owner);

        var ownShare = service.reportOwnPaid(member, result.transactionId());
        assertEquals("REPORTED_PAID", ownShare.status());
        assertEquals(member, ownShare.reportedBy());
        var ownerShare = service.reportPaid(owner, result.transactionId(), owner);
        assertEquals("REPORTED_PAID", ownerShare.status());
        assertThrows(ResponseStatusException.class,
                () -> service.reportOwnPaid(other, result.transactionId()));
        assertThrows(ResponseStatusException.class,
                () -> service.reportPaid(member, result.transactionId(), owner));
        assertEquals(1, service.history(member, group.getId()).size());
        assertEquals(2, service.history(member, group.getId()).get(0).shares().size());
    }

    @Test
    void groupEditsAreOwnerOnlyAndMaintainValidPercentages() {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        SplitGroup group = groups.saveAndFlush(new SplitGroup(owner, "Casa", List.of(
                new SplitGroup.GroupMemberInput(member, new BigDecimal("100.00")))));
        CreateSplitGroupRequestDTO update = new CreateSplitGroupRequestDTO("Viagem", List.of(
                new CreateSplitGroupRequestDTO.GroupMemberDTO(member, new BigDecimal("100.00"))));
        assertThrows(ResponseStatusException.class, () -> service.update(member, group.getId(), update));
        assertEquals("Viagem", service.update(owner, group.getId(), update).name());
        CreateSplitGroupRequestDTO invalid = new CreateSplitGroupRequestDTO("Inválido", List.of(
                new CreateSplitGroupRequestDTO.GroupMemberDTO(member, new BigDecimal("90.00"))));
        assertThrows(IllegalArgumentException.class, () -> service.update(owner, group.getId(), invalid));
    }
}
