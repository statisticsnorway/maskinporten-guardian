package no.ssb.maskinporten.guardian;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static no.ssb.maskinporten.guardian.testsupport.KeycloakDevTokenIssuer.maskinportenServiceAccountAccessToken;

@MicronautTest
public class AccessTokenControllerSecurityTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    @Disabled
    void testAccessTokenEndpointAccess() {
        String maskinportenClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        String accessToken = maskinportenServiceAccountAccessToken(maskinportenClientId, List.of( "some:scope2"));

        AccessTokenController.FetchMaskinportenAccessTokenRequest request = new AccessTokenController.FetchMaskinportenAccessTokenRequest();
        request.setScopes(Set.of("some:scope1"));

        String response = client.toBlocking()
          .retrieve(HttpRequest.POST("/access-token", request).bearerAuth(accessToken));
    }

}
