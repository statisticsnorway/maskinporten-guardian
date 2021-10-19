package no.ssb.guardian.core.cert;

import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.guardian.core.secret.SecretService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Singleton
@RequiredArgsConstructor
public class KeyStoreService {
    private final SecretService secretService;

    public KeyStoreWrapper load(String certificateSecretId, String certificatePassphraseSecretId, String keystoreEntryAlias) {
        char[] keyStorePassword = fetchKeyStorePassword(certificatePassphraseSecretId);
        KeyStore keyStore = loadKeyStore(certificateSecretId, keyStorePassword);

        return KeyStoreWrapper.builder()
          .keyStore(keyStore)
          .keyStorePassword(keyStorePassword)
          .alias(keystoreEntryAlias)
          .build();
    }

    private KeyStore loadKeyStore(String certificateSecretId, char[] keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(fetchKeyStore(certificateSecretId), keyStorePassword);
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreInitException("Error loading maskinporten guardian certificate keystore", e);
        }
    }

    private InputStream fetchKeyStore(String certificateSecretId) {
        byte[] cert = secretService.getSecret(certificateSecretId);
        return new ByteArrayInputStream(cert);
    }

    private char[] fetchKeyStorePassword(String certificatePassphraseSecretId) {
        byte[] keyStorePassword = secretService.getSecret(certificatePassphraseSecretId);
        return new String(keyStorePassword, StandardCharsets.UTF_8).toCharArray();
    }

    public static class KeyStoreInitException extends RuntimeException {
        public KeyStoreInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Builder
    @Value
    public static class KeyStoreWrapper {
        @NonNull
        private final KeyStore keyStore;

        @NonNull
        private final char[] keyStorePassword;

        @NonNull
        private final String alias;
    }

}
