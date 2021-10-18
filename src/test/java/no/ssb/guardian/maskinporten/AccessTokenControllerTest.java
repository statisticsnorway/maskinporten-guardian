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

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static no.ssb.guardian.testsupport.KeycloakDevTokenIssuer.maskinportenServiceAccountAccessToken;
import static no.ssb.guardian.testsupport.KeycloakDevTokenIssuer.personalAccessToken;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class AccessTokenControllerTest {

    private final static String MASKINPORTEN_ACCESS_TOKEN_ENDPOINT = "/maskinporten/access-token";
    private final static String MASKINPORTEN_DUMMY_ACCESS_TOKEN = "maskinporten-dummy-token";
    private final static String MASKINPORTEN_CLIENT_ID_1 = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
    private final static String MASKINPORTEN_CLIENT_ID_2 = "675c0111-2035-4d15-9cce-037f55439e80";
    private final static Set<String> DEFAULT_SCOPES = Set.of("some:scope1", "some:scope2");
    private final static Set<String> REQUESTED_SCOPES = Set.of("some:scope1");

    @Inject
    private EmbeddedServer embeddedServer;

    @Inject
    MaskinportenClientRegistry maskinportenClientRegistry;

    @MockBean(MaskinportenClientRegistry.class)
    MaskinportenClientRegistry maskinportenklientRegistry() {
        return mock(MaskinportenClientRegistry.class);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = embeddedServer.getPort();

        MaskinportenClient maskinportenClient = mock(MaskinportenClient.class);
        when(maskinportenClient.getAccessToken(anyCollection())).thenReturn(MASKINPORTEN_DUMMY_ACCESS_TOKEN);
        when(maskinportenClientRegistry.get(any())).thenReturn(maskinportenClient);
    }

    private String serviceAccountKeycloakToken() {
        return maskinportenServiceAccountAccessToken(MASKINPORTEN_CLIENT_ID_1, List.of( "some:scope1", "some:scope2"));
    }

    @Test
    void validServiceAccount_getAccessToken_shouldReturnToken() {
        given()
          .auth().oauth2(serviceAccountKeycloakToken())
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .scopes(REQUESTED_SCOPES)
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.OK.getCode())
          .body(
            "accessToken", equalTo(MASKINPORTEN_DUMMY_ACCESS_TOKEN)
          );
    }

    @Test
    void serviceAccount_getAccessTokenWithEmptyRequestBody_shouldReturnToken() {
        given()
          .auth().oauth2(serviceAccountKeycloakToken())
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder().build())
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.OK.getCode())
          .body(
            "accessToken", equalTo(MASKINPORTEN_DUMMY_ACCESS_TOKEN)
          );
    }

    @Test
    void serviceAccount_getAccessTokenWithExplicitlySpecifiedMaskinportenClientId_shouldGive400() {
        given()
          .auth().oauth2(serviceAccountKeycloakToken())
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId(MASKINPORTEN_CLIENT_ID_2)
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.BAD_REQUEST.getCode())
          .body(
            "message", equalTo("maskinportenClientId cannot be explicitly specified for service account users")
          );
    }

    @Test
    void validPersonalAccount_getAccessToken_shouldReturnToken() {
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
    }

    @Test
    void personalAccountWithoutAccess_getAccessToken_shouldGive403() {
        given()
          .auth().oauth2(personalAccessToken("Unknown", "Person"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId(MASKINPORTEN_CLIENT_ID_1)
            .scopes(REQUESTED_SCOPES)
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.FORBIDDEN.getCode())
          .body(
            "message", equalTo("unknown.person@ssb.no is not authorized to operate on behalf of maskinporten client " + MASKINPORTEN_CLIENT_ID_1)
          );
    }

    @Test
    void personalAccount_getAccessTokenForUnknownMaskinportenClient_shouldGive400() {
        given()
          .auth().oauth2(personalAccessToken("Kjell", "Fjell"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId("blah")
            .scopes(Set.of("some:scope1"))
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.BAD_REQUEST.getCode())
          .body(
            "message", startsWith("No maskinporten client configuration found for clientId=blah")
          );
    }

    @Test
    void personalAccount_getAccessTokenWithoutSpecifyingMaskinportenClient_shouldGive400() {
        given()
          .auth().oauth2(personalAccessToken("Kjell", "Fjell"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .scopes(Set.of("some:scope1"))
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.BAD_REQUEST.getCode())
          .body(
            "message", equalTo("Missing required argument: maskinportenClientId")
          );
    }

    @Test
    void personalAccount_getAccessTokenWithoutSpecifyingScopes_shouldGive400IfNotPreconfigured() {
        // Client without default scopes preconfigured
        given()
          .auth().oauth2(personalAccessToken("Kjell", "Fjell"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId(MASKINPORTEN_CLIENT_ID_1)
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .log().all()
          .statusCode(HttpStatus.BAD_REQUEST.getCode())
          .body(
            "message", equalTo("No maskinporten scopes specified")
          );

        // Client with default scopes preconfigured
        given()
          .auth().oauth2(personalAccessToken("Kjell", "Fjell"))
          .contentType(ContentType.JSON)
        .when()
          .body(FetchMaskinportenAccessTokenRequest.builder()
            .maskinportenClientId(MASKINPORTEN_CLIENT_ID_2)
            .build()
          )
          .post(MASKINPORTEN_ACCESS_TOKEN_ENDPOINT)
        .then()
          .statusCode(HttpStatus.OK.getCode())
          .body(
            "accessToken", equalTo(MASKINPORTEN_DUMMY_ACCESS_TOKEN)
          );
    }

}
