package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthRegistrationVerificationTest {
    @Mock private AppUserRepository users;
    @Mock private PasswordEncoder passwords;
    @Mock private JwtEncoder tokens;
    @Mock private CpfLookupHashService hashes;
    @Mock private ObjectProvider<TaxRegistrationVerificationPort> providers;
    @Mock private TaxRegistrationVerificationPort verifier;
    @Mock private Jwt jwt;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService(users, passwords, tokens, hashes, providers);
    }

    @Test
    void registrationFailsClosedWhenProviderIsMissing() {
        var exception = assertThrows(ResponseStatusException.class, () -> auth.register(request()));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        verify(users, never()).save(any());
    }

    @Test
    void registrationFailsClosedForNonRegularOrUnavailableCpf() {
        when(providers.getIfAvailable()).thenReturn(verifier);
        when(verifier.verifyCpf("52998224725", LocalDate.of(1990, 1, 1)))
                .thenReturn(TaxRegistrationVerificationPort.Status.NOT_REGULAR,
                        TaxRegistrationVerificationPort.Status.NOT_FOUND,
                        TaxRegistrationVerificationPort.Status.UNAVAILABLE);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                assertThrows(ResponseStatusException.class, () -> auth.register(request())).getStatusCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                assertThrows(ResponseStatusException.class, () -> auth.register(request())).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> auth.register(request())).getStatusCode());
        verify(users, never()).save(any());
    }

    @Test
    void regularCpfAllowsRegistration() {
        when(providers.getIfAvailable()).thenReturn(verifier);
        when(verifier.verifyCpf("52998224725", LocalDate.of(1990, 1, 1)))
                .thenReturn(TaxRegistrationVerificationPort.Status.REGULAR);
        when(hashes.hash("52998224725")).thenReturn("lookup");
        when(passwords.encode(any())).thenReturn("encoded");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("token");

        assertEquals("Cliente", auth.register(request()).user().name());
        verify(users).save(any());
    }

    @Test
    void existingLoginDoesNotDependOnVerificationProvider() {
        AppUser user = new AppUser("52998224725", "lookup", "Cliente", "encoded");
        when(hashes.hash("52998224725")).thenReturn("lookup");
        when(users.findByCpfLookupHash("lookup")).thenReturn(Optional.of(user));
        when(passwords.matches("senha-exemplo", "encoded")).thenReturn(true);
        when(tokens.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("token");

        assertEquals(user.getId(), auth.login(new LoginRequestDTO("52998224725", "senha-exemplo")).user().id());
        verify(providers, never()).getIfAvailable();
    }

    @Test
    void nonexistentAccountStillChecksPasswordAndIsRejected() {
        when(passwords.encode("giro-nonexistent-account-timing-padding")).thenReturn("dummy-hash");
        auth = new AuthService(users, passwords, tokens, hashes, providers);
        when(hashes.hash("52998224725")).thenReturn("lookup");
        when(users.findByCpfLookupHash("lookup")).thenReturn(Optional.empty());
        when(passwords.matches("senha-exemplo", "dummy-hash")).thenReturn(true);
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                () -> auth.login(new LoginRequestDTO("52998224725", "senha-exemplo"))).getStatusCode());
        verify(passwords).matches("senha-exemplo", "dummy-hash");
    }

    private RegisterRequestDTO request() {
        return new RegisterRequestDTO("52998224725", "Cliente", "senha-exemplo", LocalDate.of(1990, 1, 1));
    }
}
