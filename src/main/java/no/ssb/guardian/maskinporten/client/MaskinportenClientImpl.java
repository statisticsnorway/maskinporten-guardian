package no.ssb.guardian.maskinporten.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@RequiredArgsConstructor
public class MaskinportenClientImpl implements MaskinportenClient {

    private static final Logger log = LoggerFactory.getLogger(MaskinportenClientImpl.class);

    @NonNull
    private final Maskinportenklient maskinportenklient;

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    public String getAccessToken(@NonNull Set<String> scopes) {
        log.info("Requesting access token with scopes: {}", scopes);
        AccessTokenRequest request = AccessTokenRequest.builder().scopes(scopes).build();
        String token = maskinportenklient.getAccessToken(request);
        log.debug("Received access token for scopes {}: {}", scopes, token != null ? "[REDACTED]" : "null");
        return token;
    }

     /**
     * Retrieve maskinporten access token with specified scopes and access token audience
     * @param scopes
     * @param accessTokenAudience
     * @return access token that can be used to access APIs protected by maskinporten
     */
    public String getAccessToken(@NonNull Set<String> scopes, String accessTokenAudience) {
        log.info("Requesting access token with scopes: {} and audience: {}", scopes, accessTokenAudience);
        AccessTokenRequest request = AccessTokenRequest.builder()
                .scopes(scopes)
                .audience(accessTokenAudience)
                .build();
        String token = maskinportenklient.getAccessToken(request);
        log.debug("Received access token for scopes {} and audience {}: {}", scopes, accessTokenAudience, token != null ? "[REDACTED]" : "null");
        return token;
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
