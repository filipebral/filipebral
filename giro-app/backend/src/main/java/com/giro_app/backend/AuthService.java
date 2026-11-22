package com.giro_app.backend;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final long TOKEN_LIFETIME_SECONDS = 900;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final CpfLookupHashService cpfLookupHashService;

    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            CpfLookupHashService cpfLookupHashService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.cpfLookupHashService = cpfLookupHashService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha excede o limite seguro de 72 bytes.");
        }
        String cpf = normalizeCpf(request.cpf());
        String cpfLookupHash = cpfLookupHashService.hash(cpf);
        if (users.existsByCpfLookupHash(cpfLookupHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível concluir o cadastro com esses dados.");
        }

        AppUser user = users.save(new AppUser(
                cpf,
                cpfLookupHash,
                request.name().trim(),
                passwordEncoder.encode(request.password())));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        String cpf = normalizeCpf(request.cpf());
        AppUser user = users.findByCpfLookupHash(cpfLookupHashService.hash(cpf))
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "CPF ou senha inválidos."));
        return issueToken(user);
    }

    private AuthResponseDTO issueToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("giro-app")
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_LIFETIME_SECONDS, ChronoUnit.SECONDS))
                .subject(user.getId().toString())
                .audience(List.of("giro-api"))
                .claim("name", user.getName())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponseDTO(
                token,
                TOKEN_LIFETIME_SECONDS,
                new AuthResponseDTO.UserDTO(user.getId(), user.getName()));
    }

    private String normalizeCpf(String input) {
        String cpf = input.replaceAll("\\D", "");
        if (!isValidCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um CPF válido.");
        }
        return cpf;
    }

    private boolean isValidCpf(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateCpfDigit(cpf, 9);
        int secondDigit = calculateCpfDigit(cpf, 10);
        return cpf.charAt(9) - '0' == firstDigit && cpf.charAt(10) - '0' == secondDigit;
    }

    private int calculateCpfDigit(String cpf, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += (cpf.charAt(index) - '0') * (length + 1 - index);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
