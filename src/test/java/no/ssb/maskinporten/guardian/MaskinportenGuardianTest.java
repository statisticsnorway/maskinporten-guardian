package no.ssb.maskinporten.guardian;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.http.HttpStatus;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MicronautTest
class MaskinportenGuardianTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    MaskinPortenKeyStore maskinPortenKeyStore;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter mockAttemptCounter;

    @Mock
    Counter mockSuccessCounter;

    @Mock
    ClientAuthorizer clientAuthorizer;

    @Mock
    MaskinportenClientFactory maskinportenClientFactory;

    @Mock
    Maskinportenklient mockMaskinportenklient;

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
    void testGetAccesstoken(){
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));
        String mockAccessToken = "ey678hmj7798nnlll54398bgc77dgh121876whgjhgfkhgfv";

        Principal principal = () -> "ssb-service-user-1";

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        when(maskinportenClientFactory.maskinportenClient(testClientId)).thenReturn(mockMaskinportenklient);
        when(mockMaskinportenklient.getAccessToken(request.getScopes())).thenReturn(mockAccessToken);
        when(meterRegistry.counter("guardian.access", "request", "token-fetch-attempt")).thenReturn(mockAttemptCounter);
        when(meterRegistry.counter(  "guardian.access", "request", "token-fetch-success")).thenReturn(mockSuccessCounter);

        doNothing().when(mockAttemptCounter).increment();
        doNothing().when(mockSuccessCounter).increment();

        io.micronaut.http.HttpResponse<AccessTokenController.AccessTokenResponse> response = accessTokenController.fetchMaskinportenAccessToken(principal, request);

        Assertions.assertEquals(HttpStatus.OK, response.status());
        Assertions.assertNotNull(response.body());
        Assertions.assertEquals(
                mockAccessToken,
                response.body().getAccessToken());
    }

    void getKeyStoreAndCertificates() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        keyStorePassword = maskinPortenKeyStore.fetchKeyStorePassword();
        keyStore = maskinPortenKeyStore.loadKeyStore(keyStorePassword);
    }
}
