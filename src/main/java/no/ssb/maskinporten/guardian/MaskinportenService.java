package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ks.fiks.maskinporten.Maskinportenklient;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class MaskinportenService {

    private final MaskinportenClientFactory maskinportenClientFactory;

    // TODO: Consider ditching this class and inject the maskinporten client directly into the controller instead?
    public String getAccessToken(AccessTokenController.AccessTokenRequest request) {
        Maskinportenklient client = maskinportenClientFactory.maskinportenClient(request.getMaskinportenClientId());
        return client.getAccessToken(request.getScopes());
    }

}
