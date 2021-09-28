package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import no.ssb.maskinporten.guardian.config.MaskinportenConfig;

import java.security.Principal;

@Singleton
@RequiredArgsConstructor
public class ClientAuthorizer {

    private final MaskinportenConfig maskinportenConfig;

    public void validateMaskinportenClientUsageAuthorization(String maskinportenClientId, Principal principal) {
        String principalName = principal.getName();
        if (! maskinportenConfig.getClientConfig(maskinportenClientId).getAuthorizedUsers().contains(principalName)) {
            throw new NotAuthorizedForMaskinportenClientUsageException(maskinportenClientId, principalName);
        }
    }

    public static class NotAuthorizedForMaskinportenClientUsageException extends RuntimeException {
        public NotAuthorizedForMaskinportenClientUsageException(String maskinportenClientId, String principalName) {
            super(principalName + " is not authorized to operate on behalf of maskinporten client " + maskinportenClientId);
        }
    }
}
