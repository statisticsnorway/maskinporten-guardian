package no.ssb.guardian.maskinporten.client;

import java.util.Set;

public interface MaskinportenClient {

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    String getAccessToken(Set<String> scopes);

    /**
     * Retrieve maskinporten access token with specified scope and access token audience
     * @param scopes the set of scopes to request in the access token
     * @param accessTokenAudience the audience to request in the access token
     * @return access token that can be used to access APIs protected by maskinporten
     *
     */
    String getAccessToken(Set<String> scopes, String accessTokenAudience);
}
