package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * MaskinportenklientRegistry instantiates and then keeps track of instantiated maskinportenklient instances.
 *
 * <p>Each maskinportenklient instance is associated with a specific maskinportenClientId. The maskinportenklients
 * maintain a cache of already fetched tokens and determine if we actually need to request a new access token from
 * Maskinporten - or if a cached one can be used.</p>
 */
@Singleton
@RequiredArgsConstructor
public class MaskinportenklientRegistry {

    private final KeyStoreService keyStoreService;
    private final Map<String, Maskinportenklient> registry = new HashMap<>();

    /**
     * Retrieve maskinportenklient from registry if it exists - else create a new instance, add it to the registry and
     * return the new instance.
     *
     * @param maskinportenClientConfig maskinportenClientConfig holds configuration values associated with a specific
     *                                 maskinporten client that we want to retrieve a maskinportenklient instance for
     * @return a Maskinportenklient instance that can be used to access the Maskinporten API
     */
    public Maskinportenklient get(MaskinportenClientConfig maskinportenClientConfig) {
        return registry.computeIfAbsent(maskinportenClientConfig.getClientId(), maskinportenClientId -> {
            KeyStoreService.KeyStoreWrapper ks = keyStoreService.load();

            try {
                return new Maskinportenklient(ks.getKeyStore(), ks.getAlias(), ks.getKeyStorePassword(), MaskinportenklientProperties.builder()
                  .numberOfSecondsLeftBeforeExpire(maskinportenClientConfig.getNumberOfSecondsLeftBeforeExpire())
                  .issuer(maskinportenClientId)
                  .audience(maskinportenClientConfig.getAudience())
                  .tokenEndpoint(maskinportenClientConfig.getTokenEndpoint())
                  .build());
            } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
                throw new MaskinportenklientInitException("Error initializing maskinportenklient for " + maskinportenClientConfig, e);
            }
        });
    }

    public static class MaskinportenklientInitException extends RuntimeException {
        public MaskinportenklientInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
