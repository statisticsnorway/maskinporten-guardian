package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.maskinporten.guardian.config.CertificateConfig;
import no.ssb.maskinporten.guardian.secret.SecretService;

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
class KeyStoreService {
    private final SecretService secretService;
    private final CertificateConfig certificateConfig;

    KeyStoreWrapper load() {
        char[] keyStorePassword = fetchKeyStorePassword();
        KeyStore keyStore = loadKeyStore(keyStorePassword);

        return KeyStoreWrapper.builder()
          .keyStore(keyStore)
          .keyStorePassword(keyStorePassword)
          .alias(certificateConfig.getCertificateKeystoreEntryAlias())
          .build();
    }

    private InputStream fetchKeyStore() {
        byte[] cert = secretService.getSecret(certificateConfig.getCertificateSecretId());
        return new ByteArrayInputStream(cert);
    }

    private char[] fetchKeyStorePassword() {
        byte[] keyStorePassword = secretService.getSecret(certificateConfig.getCertificatePassphraseSecretId());
        return new String(keyStorePassword, StandardCharsets.UTF_8).toCharArray();
    }

    private KeyStore loadKeyStore(char[] keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(fetchKeyStore(), keyStorePassword);
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreInitException("Error loading maskinporten guardian certificate keystore", e);
        }
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
