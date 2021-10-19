package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.maskinporten.guardian.config.CertificateConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenConfig;

import java.security.KeyStore;

@Singleton
@RequiredArgsConstructor
public class MaskinportenClientFactory {

    private final MaskinportenConfig maskinportenConfig;
    private final MaskinportenKeyStore maskinPortenKeyStore;
    private final CertificateConfig certificateConfig;

    // TODO: Keep client instances in a timed cache?
    @SneakyThrows
    public Maskinportenklient maskinportenClient(String clientId) {
        char[] keyStorePassword = maskinPortenKeyStore.fetchKeyStorePassword();
        KeyStore keyStore = maskinPortenKeyStore.loadKeyStore(keyStorePassword);

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