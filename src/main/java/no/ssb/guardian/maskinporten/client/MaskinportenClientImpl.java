package no.ssb.guardian.maskinporten.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.Maskinportenklient;

import java.util.Collection;
import java.util.Set;

@RequiredArgsConstructor
public class MaskinportenClientImpl implements MaskinportenClient {

    @NonNull
    private final Maskinportenklient maskinportenklient;

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    public String getAccessToken(@NonNull Set<String> scopes) {
        AccessTokenRequest request = AccessTokenRequest.builder().scopes(scopes).build();
        return maskinportenklient.getAccessToken(request);
    }

}
