package com.giro_app.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequestDTO(
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull @Past LocalDate birthDate
) {
}
