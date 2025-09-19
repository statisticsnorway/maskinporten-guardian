package no.ssb.guardian.maskinporten.client;

import java.util.Set;

public interface MaskinportenClient {

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    String getAccessToken(Set<String> scopes);

    /**
     * Retrieve maskinporten access token with specifiecd scope and access token audience
     *
     */
    String getAccessToken(Set<String> scopes, String accessTokenAudience);
}
