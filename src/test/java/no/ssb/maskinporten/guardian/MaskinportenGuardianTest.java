package no.ssb.maskinporten.guardian;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.http.HttpStatus;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.*;
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
    MaskinportenClientFactory maskinportenClientFactory;

    @Inject
    AccessTokenController accessTokenController;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAccessToken(){
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));
        String mockAccessToken = "ey678hmj7798nnlll54398bgc77dgh121876whgjhgfkhgfv";

        Maskinportenklient mockMaskinportenClient = mock(Maskinportenklient.class);

        Principal principal = () -> "ssb-service-user-1";

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        when(maskinportenClientFactory.maskinportenClient(testClientId)).thenReturn(mockMaskinportenClient);
        when(mockMaskinportenClient.getAccessToken(request.getScopes())).thenReturn(mockAccessToken);

        io.micronaut.http.HttpResponse<AccessTokenController.AccessTokenResponse> response = accessTokenController.fetchMaskinportenAccessToken(principal, request);

        Assertions.assertEquals(HttpStatus.OK, response.status());
        Assertions.assertNotNull(response.body());
        Assertions.assertEquals(
                mockAccessToken,
                response.body().getAccessToken());

    }

    @Test
    void testGetAccessTokenUnauthorizedAccess(){
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        Principal principal = () -> "ssb-service-user-3";

        Assertions.assertThrows(ClientAuthorizer.NotAuthorizedForMaskinportenClientUsageException.class, () -> {
            accessTokenController.fetchMaskinportenAccessToken(principal, request);
        });
    }

    @Test
    void testGetAccessTokenInvalidId(){
        String testClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80x";
        Set<String> scopes = new HashSet<>(Arrays.asList("scope:test"));

        AccessTokenController.AccessTokenRequest request = new AccessTokenController.AccessTokenRequest();
        request.setScopes(scopes);
        request.setMaskinportenClientId(testClientId);

        Principal principal = () -> "ssb-service-user-1";

        Assertions.assertThrows(MaskinportenClientConfig.NotFoundException.class, () -> {
            accessTokenController.fetchMaskinportenAccessToken(principal, request);
        });
    }

    @MockBean(MaskinportenClientFactory.class)
    MaskinportenClientFactory maskinportenClientFactory(){
        return mock(MaskinportenClientFactory.class);
    }

}
