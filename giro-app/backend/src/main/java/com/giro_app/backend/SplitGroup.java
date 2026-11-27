package com.giro_app.backend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

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
        this.name = validateName(name);
        
        validateAndAddMembers(inputMembers);
    }

    public void update(String name, List<GroupMemberInput> inputMembers) {
        this.name = validateName(name);
        this.members.clear();
        validateAndAddMembers(inputMembers);
    }

    private String validateName(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 80) {
            throw new IllegalArgumentException("O nome do grupo deve ter entre 1 e 80 caracteres.");
        }
        return value.trim();
    }

    private void validateAndAddMembers(List<GroupMemberInput> inputMembers) {
        if (inputMembers == null || inputMembers.isEmpty()) {
            throw new IllegalArgumentException("O grupo de divisão deve ter pelo menos um membro.");
        }

        Set<UUID> memberIds = new HashSet<>();
        for (GroupMemberInput input : inputMembers) {
            if (input == null || input.userId() == null || input.percentage() == null
                    || input.percentage().signum() <= 0 || input.percentage().scale() > 2
                    || !memberIds.add(input.userId())) {
                throw new IllegalArgumentException("Participantes devem ser únicos e ter cotas positivas de até duas casas decimais.");
            }
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