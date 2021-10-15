package no.ssb.maskinporten.guardian;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.http.HttpStatus;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MicronautTest
@Disabled
class MaskinportenGuardianTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    KeyStoreService maskinportenKeyStore;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter mockAttemptCounter;

    @Mock
    Counter mockSuccessCounter;

    @Mock
    ClientAuthorizer clientAuthorizer;

    @Mock
    MaskinportenklientRegistry maskinportenklientRegistry;

    @Mock
    Maskinportenklient mockMaskinportenklient;

    @InjectMocks
    AccessTokenController accessTokenController;

    KeyStoreService.KeyStoreWrapper keyStoreWrapper;

    @BeforeEach
    void setup() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        getKeyStoreAndCertificates();
    }

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAccessToken(){
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));
        String mockAccessToken = "ey678hmj7798nnlll54398bgc77dgh121876whgjhgfkhgfv";

        Principal principal = () -> "ssb-service-user-1";

        AccessTokenController.FetchMaskinportenAccessTokenRequest request = new AccessTokenController.FetchMaskinportenAccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        when(maskinportenklientRegistry.get(Mockito.any())).thenReturn(mockMaskinportenklient);
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

    void getKeyStoreAndCertificates() {
        keyStoreWrapper = maskinportenKeyStore.load();
    }


}
