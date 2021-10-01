package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.maskinporten.guardian.config.CertificateConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenConfig;
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
public class MaskinportenClientFactory {

    private final SecretService secretService;
    private final CertificateConfig certificateConfig;
    private final MaskinportenConfig maskinportenConfig;

    private KeyStore loadKeyStore(char[] keyStorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(fetchKeyStore(), keyStorePassword);
        return keyStore;
    }

    InputStream fetchKeyStore() {
        byte[] cert = secretService.getSecret(certificateConfig.getCertificateSecretId());
        return new ByteArrayInputStream(cert);
    }

    char[] fetchKeyStorePassword() {
        byte[] keyStorePassword = secretService.getSecret(certificateConfig.getCertificatePassphraseSecretId());
        return new String(keyStorePassword, StandardCharsets.UTF_8).toCharArray();
    }

    // TODO: Keep client instances in a timed cache?
    @SneakyThrows
    public Maskinportenklient maskinportenClient(String clientId) {
        char[] keyStorePassword = fetchKeyStorePassword();
        KeyStore keyStore = loadKeyStore(keyStorePassword);

        MaskinportenClientConfig clientConfig = maskinportenConfig.getClientConfig(clientId);
        Maskinportenklient maskinporten = new Maskinportenklient(keyStore, certificateConfig.getCertificateKeystoreEntryAlias(), keyStorePassword, MaskinportenklientProperties.builder()
          .numberOfSecondsLeftBeforeExpire(clientConfig.getNumberOfSecondsLeftBeforeExpire())
          .issuer(clientId)
          .audience(clientConfig.getAudience())
          .tokenEndpoint(clientConfig.getTokenEndpoint())
          .build());
        return maskinporten;
    }
}
