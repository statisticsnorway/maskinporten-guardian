package no.ssb.guardian.maskinporten.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.ks.fiks.maskinporten.Maskinportenklient;

import java.util.Collection;

@RequiredArgsConstructor
public class MaskinportenClientImpl implements MaskinportenClient {

    @NonNull
    private final Maskinportenklient maskinportenklient;

    /**
     * Retrieve maskinporten access token with specified scopes
     */
    public String getAccessToken(@NonNull Collection<String> scopes) {
        return maskinportenklient.getAccessToken(scopes);
    }

}
