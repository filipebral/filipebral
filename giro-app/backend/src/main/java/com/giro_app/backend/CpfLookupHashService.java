package com.giro_app.backend;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CpfLookupHashService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final byte[] KEY_CONTEXT = "giro-app:cpf-lookup:v1".getBytes(StandardCharsets.UTF_8);
    private final byte[] lookupKey;

    public CpfLookupHashService(@Value("${app.security.encryption.key-base64}") String encryptionKeyBase64) {
        byte[] encryptionKey = Base64.getDecoder().decode(encryptionKeyBase64);
        if (encryptionKey.length != 32) {
            throw new IllegalStateException("GIRO_ENCRYPTION_KEY_BASE64 must contain exactly 32 bytes.");
        }
        try {
            Mac keyDerivation = Mac.getInstance(HMAC_ALGORITHM);
            keyDerivation.init(new SecretKeySpec(encryptionKey, HMAC_ALGORITHM));
            this.lookupKey = keyDerivation.doFinal(KEY_CONTEXT);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to initialize the CPF lookup key.", exception);
        }
    }

    public String hash(String normalizedCpf) {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(new SecretKeySpec(lookupKey, HMAC_ALGORITHM));
            return HexFormat.of().formatHex(hmac.doFinal(normalizedCpf.getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to create the CPF lookup index.", exception);
        }
    }
}
