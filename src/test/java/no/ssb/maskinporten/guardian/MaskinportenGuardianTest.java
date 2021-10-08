package no.ssb.maskinporten.guardian;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.maskinporten.guardian.config.CertificateConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@MicronautTest
class MaskinportenGuardianTest {

    @Inject
    EmbeddedApplication<?> application;

    @InjectMocks
    AccessTokenController accessTokenController;

    @Inject
    MaskinPortenKeyStore maskinPortenKeyStore;

    @Inject
    CertificateConfig certificateConfig;

    @Inject
    MaskinportenConfig maskinportenConfig;

    char[] keyStorePassword;
    KeyStore keyStore;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAccesstoken() throws CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, IOException {
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));
        String mockAccessToken = "ljhkghjhghgafshgafsdjhgafsdgh121876whgjhgfkhgfv";

        Maskinportenklient maskinportenklient = mock(Maskinportenklient.class);
        MaskinportenClientFactory maskinportenClientFactory = mock(MaskinportenClientFactory.class);

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        MaskinportenClientConfig clientConfig = maskinportenConfig.getClientConfig(request.getMaskinportenClientId());

        getKeyStoreAndCertificates();

        Maskinportenklient maskinportenClient = new Maskinportenklient(keyStore, certificateConfig.getCertificateKeystoreEntryAlias(), keyStorePassword,  MaskinportenklientProperties.builder()
                .numberOfSecondsLeftBeforeExpire(clientConfig.getNumberOfSecondsLeftBeforeExpire())
                .issuer(testClientId)
                .audience(clientConfig.getAudience())
                .tokenEndpoint(clientConfig.getTokenEndpoint())
                .build());

        Principal principal = new Principal() {
            @Override
            public String getName() {
                return "ssb-service-user-1";
            }
        };

        when(maskinportenClientFactory.maskinportenClient(testClientId)).thenReturn(maskinportenClient);
        when(maskinportenklient.getAccessToken(request.getScopes())).thenReturn(mockAccessToken);

        accessTokenController.fetchMaskinportenAccessToken(principal, request);

    }

    void getKeyStoreAndCertificates() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        keyStorePassword = maskinPortenKeyStore.fetchKeyStorePassword();
        keyStore = maskinPortenKeyStore.loadKeyStore(keyStorePassword);
    }

}
