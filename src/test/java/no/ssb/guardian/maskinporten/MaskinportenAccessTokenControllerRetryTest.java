package no.ssb.guardian.maskinporten;

import io.micronaut.http.HttpStatus;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import no.ssb.guardian.maskinporten.MaskinportenAccessTokenController.FetchMaskinportenAccessTokenRequest;
import no.ssb.guardian.maskinporten.client.MaskinportenClient;
import no.ssb.guardian.maskinporten.client.MaskinportenClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static no.ssb.guardian.testsupport.KeycloakDevTokenIssuer.personalAccessToken;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@MicronautTest
public class MaskinportenAccessTokenControllerRetryTest {

    private final static String MASKINPORTEN_ACCESS_TOKEN_ENDPOINT = "/maskinporten/access-token";
    private final static String MASKINPORTEN_DUMMY_ACCESS_TOKEN = "maskinporten-dummy-token";
    private final static String MASKINPORTEN_CLIENT_ID_1 = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
    private final static Set<String> REQUESTED_SCOPES = Set.of("some:scope1");

    @Inject
    private EmbeddedServer embeddedServer;

    @Inject
    MaskinportenClientRegistry maskinportenClientRegistry;

    @MockBean(MaskinportenClientRegistry.class)
    MaskinportenClientRegistry maskinportenklientRegistry() {
        return mock(MaskinportenClientRegistry.class);
    }

    private MaskinportenClient maskinportenClientMock = mock(MaskinportenClient.class);
    @BeforeEach
    void setUp() {
        RestAssured.port = embeddedServer.getPort();
        when(maskinportenClientRegistry.get(any())).thenReturn(maskinportenClientMock);
    }

    @Test
    void validRequest_shouldRetryOnConnectionError() {
        when(maskinportenClientMock.getAccessToken(anySet()))
                .thenThrow(new RuntimeException(new SocketException(("Connection reset"))))
                .thenReturn(MASKINPORTEN_DUMMY_ACCESS_TOKEN);

        given()
          .auth().oauth2(personalAccessToken("Kjell", "Fjell"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId(MASKINPORTEN_CLIENT_ID_1)
            .scopes(REQUESTED_SCOPES)
            .build()
            )
            .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.OK.getCode())
          .body(
              "accessToken", equalTo(MASKINPORTEN_DUMMY_ACCESS_TOKEN)
          );
        verify(maskinportenClientMock, times(2)).getAccessToken(anySet());
    }
}
