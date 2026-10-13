package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SplitGroupResponseDTO(
        UUID id,
        String name,
        UUID ownerUserId,
        List<MemberDTO> members
) {
    public static SplitGroupResponseDTO from(SplitGroup group) {
        List<MemberDTO> members = group.getMembers().stream()
                .map(member -> new MemberDTO(member.getId(), member.getUserId(), member.getPercentage()))
                .toList();
        return new SplitGroupResponseDTO(group.getId(), group.getName(), group.getOwnerUserId(), members);
    }

    public record MemberDTO(UUID id, UUID userId, BigDecimal percentage) {
    }
}
