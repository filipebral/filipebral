package com.giro_app.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Size(max = 72) String password
) {
}
