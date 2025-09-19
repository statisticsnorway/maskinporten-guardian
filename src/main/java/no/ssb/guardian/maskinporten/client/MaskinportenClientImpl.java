package no.ssb.guardian.maskinporten.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.Maskinportenklient;

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

     /**
     * Retrieve maskinporten access token with specified scopes and access token audience
     * @param scopes
     * @param accessTokenAudience
     * @return access token that can be used to access APIs protected by maskinporten
     */
    public String getAccessToken(@NonNull Set<String> scopes, String accessTokenAudience) {
        AccessTokenRequest request = AccessTokenRequest.builder()
                .scopes(scopes)
                .audience(accessTokenAudience)
                .build();
        return maskinportenklient.getAccessToken(request);
    }

}
