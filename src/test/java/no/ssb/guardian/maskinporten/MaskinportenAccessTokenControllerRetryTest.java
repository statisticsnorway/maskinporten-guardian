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
import org.junit.jupiter.api.DisplayName;

import java.net.SocketException;
import java.net.ConnectException;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static no.ssb.guardian.testsupport.KeycloakDevTokenIssuer.personalAccessToken;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * Test class for verifying retry behavior of MaskinportenAccessTokenController
 * when encountering network connectivity issues.
 *
 * This test suite validates that the controller properly handles transient
 * network failures and implements appropriate retry mechanisms.
 */
@MicronautTest
public class MaskinportenAccessTokenControllerRetryTest {

    private final static String MASKINPORTEN_ACCESS_TOKEN_ENDPOINT = "/maskinporten/access-token";
    private final static String MASKINPORTEN_DUMMY_ACCESS_TOKEN = "maskinporten-dummy-token";
    private final static String MASKINPORTEN_BACKUP_ACCESS_TOKEN = "maskinporten-backup-token";
    private final static String MASKINPORTEN_CLIENT_ID_1 = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
    private final static String MASKINPORTEN_CLIENT_ID_2 = "8fb54c87-7c8e-5af9-bg3c-5225fcc77d91";
    private final static Set<String> REQUESTED_SCOPES = Set.of("some:scope1");
    private final static Set<String> EXTENDED_SCOPES = Set.of("some:scope1", "some:scope2", "admin:read");

    // Test user constants for better test readability
    private final static String TEST_USER_FIRST_NAME = "Kjell";
    private final static String TEST_USER_LAST_NAME = "Fjell";
    private final static String TEST_USER_ALT_FIRST_NAME = "Ola";
    private final static String TEST_USER_ALT_LAST_NAME = "Nordmann";

    @Inject
    private EmbeddedServer embeddedServer;

    @Inject
    MaskinportenClientRegistry maskinportenClientRegistry;

    @MockBean(MaskinportenClientRegistry.class)
    MaskinportenClientRegistry maskinportenklientRegistry() {
        return mock(MaskinportenClientRegistry.class);
    }

    private MaskinportenClient maskinportenClientMock = mock(MaskinportenClient.class);
    private MaskinportenClient alternativeMaskinportenClientMock = mock(MaskinportenClient.class);

    @BeforeEach
    void setUp() {
        RestAssured.port = embeddedServer.getPort();
        when(maskinportenClientRegistry.get(any())).thenReturn(maskinportenClientMock);

        // Reset mocks to ensure clean state for each test
        reset(maskinportenClientMock, alternativeMaskinportenClientMock);
    }

    @Test
    @DisplayName("Should retry on SocketException and succeed on second attempt")
    void validRequest_shouldRetryOnConnectionError() {
        when(maskinportenClientMock.getAccessToken(anySet()))
                .thenThrow(new RuntimeException(new SocketException(("Connection reset"))))
                .thenReturn(MASKINPORTEN_DUMMY_ACCESS_TOKEN);

        given()
                .auth().oauth2(personalAccessToken(TEST_USER_FIRST_NAME, TEST_USER_LAST_NAME))
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

    @Test
    @DisplayName("Should handle multiple retry scenarios with different client configurations")
    void validRequest_shouldHandleMultipleRetryScenarios() {
        // This test validates that the retry mechanism works consistently
        // across different client configurations and scope combinations
        when(maskinportenClientMock.getAccessToken(eq(REQUESTED_SCOPES)))
                .thenThrow(new RuntimeException(new SocketException("Network unreachable")))
                .thenReturn(MASKINPORTEN_DUMMY_ACCESS_TOKEN);

        given()
                .auth().oauth2(personalAccessToken(TEST_USER_FIRST_NAME, TEST_USER_LAST_NAME))
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

        // Verify that the retry mechanism was invoked exactly twice
        verify(maskinportenClientMock, times(2)).getAccessToken(eq(REQUESTED_SCOPES));
    }
}
