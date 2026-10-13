package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSplitGroupRequestDTO(
    String name,
    List<GroupMemberDTO> members
) {
    public record GroupMemberDTO(
        UUID userId,
        BigDecimal percentage
    ) {}
}