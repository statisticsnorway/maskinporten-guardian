package no.ssb.guardian.maskinporten;

import jakarta.inject.Singleton;
import no.ssb.guardian.maskinporten.config.MaskinportenClientConfig;

@Singleton
public class ClientAuthorizer {

    public void validateMaskinportenClientUsageAuthorization(MaskinportenClientConfig clientConfig, String principalName) {
        if (! clientConfig.getAuthorizedUsers().contains(principalName)) {
            throw new NotAuthorizedForMaskinportenClientUsageException(clientConfig.getClientId(), principalName);
        }
    }

    public static class NotAuthorizedForMaskinportenClientUsageException extends RuntimeException {
        public NotAuthorizedForMaskinportenClientUsageException(String maskinportenClientId, String principalName) {
            super(principalName + " is not authorized to operate on behalf of maskinporten client " + maskinportenClientId);
        }
    }

}
