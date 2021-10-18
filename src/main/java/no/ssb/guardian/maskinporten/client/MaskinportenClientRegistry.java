package no.ssb.guardian.maskinporten.client;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.guardian.core.cert.KeyStoreService;
import no.ssb.guardian.core.cert.KeyStoreService.KeyStoreWrapper;
import no.ssb.guardian.core.cert.CertificateConfig;
import no.ssb.guardian.maskinporten.config.MaskinportenClientConfig;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * MaskinportenClientRegistry instantiates and then keeps track of instantiated MaskinportenClient instances.
 *
 * <p>Each MaskinportenClient delegates to a no.ks.fiks.maskinporten.Maskinportenklient instance. It is associated
 * with a specific maskinportenClientId. The Maskinportenklients maintain a cache of already fetched tokens and
 * determine if we actually need to request a new access token from Maskinporten - or if a cached one can be used.</p>
 */
@Singleton
@RequiredArgsConstructor
public class MaskinportenClientRegistry {

    @Named("ssb-maskinporten-virksomhetssertifikat")
    private final CertificateConfig certificateConfig;

    private final KeyStoreService keyStoreService;

    private final Map<String, MaskinportenClient> registry = new HashMap<>();

    /**
     * Retrieve maskinportenClient from registry if it exists - else create a new instance, add it to the registry and
     * return the new instance.
     *
     * @param maskinportenClientConfig maskinportenClientConfig holds configuration values associated with a specific
     *                                 maskinporten client that we want to retrieve a maskinportenClient instance for
     * @return a MaskinportenClient instance that can be used to access the Maskinporten API
     */
    public MaskinportenClient get(MaskinportenClientConfig maskinportenClientConfig) {
        return registry.computeIfAbsent(maskinportenClientConfig.getClientId(), maskinportenClientId -> {
            KeyStoreWrapper ks = loadKeyStore();

            try {
                Maskinportenklient klient = new Maskinportenklient(ks.getKeyStore(), ks.getAlias(), ks.getKeyStorePassword(), MaskinportenklientProperties.builder()
                  .numberOfSecondsLeftBeforeExpire(maskinportenClientConfig.getNumberOfSecondsLeftBeforeExpire())
                  .issuer(maskinportenClientId)
                  .audience(maskinportenClientConfig.getAudience())
                  .tokenEndpoint(maskinportenClientConfig.getTokenEndpoint())
                  .build());
                return new MaskinportenClientImpl(klient);
            } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
                throw new MaskinportenClientInitException("Error initializing " + Maskinportenklient.class.getName() + " for " + maskinportenClientConfig, e);
            }
        });
    }

    KeyStoreWrapper loadKeyStore() {
        return keyStoreService.load(certificateConfig.getCertificateSecretId(), certificateConfig.getCertificatePassphraseSecretId(), certificateConfig.getCertificateKeystoreEntryAlias());
    }

    public static class MaskinportenClientInitException extends RuntimeException {
        public MaskinportenClientInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
