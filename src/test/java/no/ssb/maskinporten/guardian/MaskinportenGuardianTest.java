package no.ssb.maskinporten.guardian;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micronaut.context.annotation.Bean;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ssb.maskinporten.guardian.config.CertificateConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.config.MaskinportenConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MicronautTest
class MaskinportenGuardianTest {

    @Mock
    EmbeddedApplication<?> application;

    @Inject
    MaskinPortenKeyStore maskinPortenKeyStore;

    @Inject
    CertificateConfig certificateConfig;

    @Inject
    MaskinportenConfig maskinportenConfig;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    ClientAuthorizer clientAuthorizer;

    @Mock
    MaskinportenService maskinportenService;

    @Mock
    MaskinportenClientFactory maskinportenClientFactory;

    @InjectMocks
    AccessTokenController accessTokenController;

    char[] keyStorePassword;
    KeyStore keyStore;

    @BeforeEach
    void setup() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        getKeyStoreAndCertificates();
    }

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAccesstoken() throws CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, IOException {
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));
        String mockAccessToken = "ey678hmj7798nnlll54398bgc77dgh121876whgjhgfkhgfv";

        Principal principal = new Principal() {
            @Override
            public String getName() {
                return "ssb-service-user-1";
            }
        };

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        MaskinportenClientConfig clientConfig = maskinportenConfig.getClientConfig(testClientId);

        Maskinportenklient testMaskinportenClient = new Maskinportenklient(keyStore, certificateConfig.getCertificateKeystoreEntryAlias(), keyStorePassword,  MaskinportenklientProperties.builder()
                .numberOfSecondsLeftBeforeExpire(clientConfig.getNumberOfSecondsLeftBeforeExpire())
                .issuer(testClientId)
                .audience(clientConfig.getAudience())
                .tokenEndpoint(clientConfig.getTokenEndpoint())
                .build());

        when(maskinportenClientFactory.maskinportenClient(request.getMaskinportenClientId())).thenReturn(testMaskinportenClient);
        when(maskinportenService.getAccessToken(request)).thenReturn(mockAccessToken);

        accessTokenController.fetchMaskinportenAccessToken(principal, request);
    }

    void getKeyStoreAndCertificates() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        keyStorePassword = maskinPortenKeyStore.fetchKeyStorePassword();
        keyStore = maskinPortenKeyStore.loadKeyStore(keyStorePassword);
    }

}
