package com.giro_app.backend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "split_groups")
@Getter
@NoArgsConstructor
public class SplitGroup {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "splitGroup")
    private List<GroupMember> members = new ArrayList<>();

    public SplitGroup(UUID ownerUserId, String name, List<GroupMemberInput> inputMembers) {
        this.id = UUID.randomUUID();
        this.ownerUserId = ownerUserId;
        this.name = name;
        
        validateAndAddMembers(inputMembers);
    }

    private void validateAndAddMembers(List<GroupMemberInput> inputMembers) {
        if (inputMembers == null || inputMembers.isEmpty()) {
            throw new IllegalArgumentException("O grupo de divisão deve ter pelo menos um membro.");
        }

        BigDecimal totalPercentage = inputMembers.stream()
                .map(GroupMemberInput::percentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Compara se o total é diferente de 100.00
        if (totalPercentage.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("A soma das porcentagens do grupo deve ser exatamente 100%. Total atual: " + totalPercentage);
        }

        for (var input : inputMembers) {
            GroupMember member = new GroupMember(UUID.randomUUID(), input.userId(), input.percentage());
            member.setSplitGroup(this);
            this.members.add(member);
        }
    }

    // Record auxiliar interno para receber os dados limpios de entrada
    public record GroupMemberInput(UUID userId, BigDecimal percentage) {}
}