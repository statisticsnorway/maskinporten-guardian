package no.ssb.maskinporten.guardian;

import io.micronaut.security.authentication.ServerAuthentication;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Builder(access = AccessLevel.PRIVATE)
@Value
public class KeycloakTokenAttributes {

    @NonNull
    private final MaskinportenGuardianUserType userType;
    @NonNull
    private final String preferredUsername;
    @NonNull
    private final String keycloakClientId;

    private final String maskinportenClientId;
    private final String maskinportenAudience;
    private final Set<String> maskinportenDefaultScopes;

    public static KeycloakTokenAttributes parse(Principal principal) {
        if (! (principal instanceof ServerAuthentication)) {
            throw new KeycloakTokenParseException("Unable to parse keycloak token attributes from " + principal);
        }

        Map<String, Object> claims = ((ServerAuthentication) principal).getAttributes();
        return KeycloakTokenAttributes.builder()
          .preferredUsername((String) claims.get(Claim.PREFERRED_USERNAME))
          .keycloakClientId((String) claims.get(Claim.CLIENT_ID))
          .maskinportenClientId((String) claims.get(Claim.MASKINPORTEN_CLIENT_ID))
          .maskinportenAudience((String) claims.get(Claim.MASKINPORTEN_AUDIENCE))
          .maskinportenDefaultScopes(new HashSet<>((List<String>) claims.get(Claim.MASKINPORTEN_DEFAULT_SCOPES)))
          .userType(claims.containsKey(Claim.MASKINPORTEN_CLIENT_ID) ? MaskinportenGuardianUserType.MASKINPORTEN_GUARDIAN_SERVICE_ACCOUNT : MaskinportenGuardianUserType.PERSON)
          .build();
    }

    public Optional<String> getMaskinportenClientId() {
        return Optional.ofNullable(maskinportenClientId);
    }

    public Optional<String> getMaskinportenAudience() {
        return Optional.ofNullable(maskinportenAudience);
    }

    public Optional<Set<String>> getMaskinportenDefaultScopes() {
        return Optional.ofNullable(maskinportenDefaultScopes);
    }

    public static class KeycloakTokenParseException extends RuntimeException {
        public KeycloakTokenParseException(String message) {
            super(message);
        }
    }

    private static class Claim {
        static final String MASKINPORTEN_CLIENT_ID = "maskinporten_client_id";
        static final String MASKINPORTEN_AUDIENCE = "maskinporten_audience";
        static final String MASKINPORTEN_DEFAULT_SCOPES = "maskinporten_default_scopes";
        static final String PREFERRED_USERNAME = "preferred_username";
        static final String CLIENT_ID = "clientId";
    }

}
