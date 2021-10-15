package no.ssb.maskinporten.guardian.testsupport;

import org.junit.jupiter.api.Test;

import java.util.List;

public class KeycloakDevTokenIssuerTest {

    @Test
    void maskinportenServiceAccountAccessToken() {
        String maskinportenClientId = "7ea43b76-6b7d-49e8-af2b-4114ebb66c80";
        String token = KeycloakDevTokenIssuer.maskinportenServiceAccountAccessToken(maskinportenClientId, List.of("acme:scope1", "acme:scope2"));
    }

    @Test
    void jupyterPersonalAccessToken() {
        String token = KeycloakDevTokenIssuer.jupyterPersonalAccessToken("Kjell", "Fjell");
    }

}
