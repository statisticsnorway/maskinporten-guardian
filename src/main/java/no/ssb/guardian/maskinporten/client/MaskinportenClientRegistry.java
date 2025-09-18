package no.ssb.guardian.maskinporten.client;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.guardian.core.cert.CertificateService;
import no.ssb.guardian.maskinporten.config.MaskinportenClientConfig;

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
@Slf4j
public class MaskinportenClientRegistry {

    private final CertificateService certificateService;

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
            try {
                CertificateService.CertificateWrapper cw = certificateService.loadCertificate();
                certificateService.validateCertificate(cw.getCertificate());

                String tokenEndpoint;
                String audience = maskinportenClientConfig.getAudience();
                if (audience != null && audience.contains("skyporten")) {
                    tokenEndpoint = maskinportenClientConfig.getSkyportenTokenEndpoint();
                } else {
                    tokenEndpoint = maskinportenClientConfig.getMaskinportenTokenEndpoint();
                }

                Maskinportenklient klient = Maskinportenklient.builder()
                        .withPrivateKey(cw.getPrivateKey())
                        .withProperties(MaskinportenklientProperties.builder()
                                .numberOfSecondsLeftBeforeExpire(maskinportenClientConfig.getNumberOfSecondsLeftBeforeExpire())
                                .issuer(maskinportenClientId)
                                .audience(maskinportenClientConfig.getAudience())
                                .tokenEndpoint(tokenEndpoint)
                                .build())
                        .usingVirksomhetssertifikat(cw.getCertificate())
                        .build();
                return new MaskinportenClientImpl(klient);
            } catch (Exception e) {
                throw new MaskinportenClientInitException("Error initializing " + Maskinportenklient.class.getName() + " for " + maskinportenClientConfig, e);
            }
        });
    }

    public static class MaskinportenClientInitException extends RuntimeException {
        public MaskinportenClientInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
