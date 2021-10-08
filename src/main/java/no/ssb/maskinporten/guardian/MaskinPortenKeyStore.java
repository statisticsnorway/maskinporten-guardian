package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
public class MaskinPortenKeyStore {
    private final SecretService secretService;
    private final CertificateConfig certificateConfig;

    InputStream fetchKeyStore() {
        byte[] cert = secretService.getSecret(certificateConfig.getCertificateSecretId());
        return new ByteArrayInputStream(cert);
    }

    char[] fetchKeyStorePassword() {
        byte[] keyStorePassword = secretService.getSecret(certificateConfig.getCertificatePassphraseSecretId());
        return new String(keyStorePassword, StandardCharsets.UTF_8).toCharArray();
    }

    KeyStore loadKeyStore(char[] keyStorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(fetchKeyStore(), keyStorePassword);
        return keyStore;
    }
}
