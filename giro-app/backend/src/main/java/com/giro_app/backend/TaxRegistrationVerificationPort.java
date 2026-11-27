package com.giro_app.backend;

import java.time.LocalDate;

/**
 * A provider must match the requested identifier and, for CPF, the birth date
 * before reporting a regular registration. Unavailable or inconclusive results
 * must never be reported as regular. For CNPJ, REGULAR means cadastral status active.
 */
public interface TaxRegistrationVerificationPort {
    enum Status { REGULAR, NOT_REGULAR, NOT_FOUND, UNAVAILABLE }

    Status verifyCpf(String normalizedCpf, LocalDate birthDate);
    Status verifyCnpj(String normalizedCnpj);
}
