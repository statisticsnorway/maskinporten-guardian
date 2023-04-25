package no.ssb.guardian.maskinporten.client;

import java.util.Collection;
import java.util.Set;

public interface MaskinportenClient {

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    String getAccessToken(Set<String> scopes);

}
