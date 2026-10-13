package com.giro_app.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/split-groups")
public class SplitGroupController {

    private final CreateSplitGroupUseCase createSplitGroupUseCase;

    public SplitGroupController(CreateSplitGroupUseCase createSplitGroupUseCase) {
        this.createSplitGroupUseCase = createSplitGroupUseCase;
    }

    @PostMapping
    public ResponseEntity<SplitGroupResponseDTO> createGroup(
            @RequestBody CreateSplitGroupRequestDTO request,
            @AuthenticationPrincipal Jwt principal
    ) {
        SplitGroup createdGroup = createSplitGroupUseCase.execute(
                request,
                UUID.fromString(principal.getSubject()));
        return ResponseEntity.ok(SplitGroupResponseDTO.from(createdGroup));
    }
}
