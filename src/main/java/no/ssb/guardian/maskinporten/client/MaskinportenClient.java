package no.ssb.guardian.maskinporten.client;

import java.util.Collection;

public interface MaskinportenClient {

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    String getAccessToken(Collection<String> scopes);

}
