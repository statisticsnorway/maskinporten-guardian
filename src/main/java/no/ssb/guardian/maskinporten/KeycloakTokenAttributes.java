package no.ssb.guardian.maskinporten;

import io.micronaut.security.authentication.ServerAuthentication;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Builder(access = AccessLevel.PRIVATE)
@Value
@Slf4j
public class KeycloakTokenAttributes {

    @NonNull
    private final MaskinportenGuardianUserType userType;

    @NonNull
    private final String sub;

    private final String keycloakClientId;

    private final String maskinportenClientId;
    private final String maskinportenAudience;
    private final Set<String> maskinportenDefaultScopes;

    private final String skyportenAudience;

    public static KeycloakTokenAttributes parse(Principal principal) {
        if (!(principal instanceof ServerAuthentication)) {
            throw new KeycloakTokenParseException("Unable to parse keycloak token attributes from " + principal);
        }

        Map<String, Object> claims = ((ServerAuthentication) principal).getAttributes();

        log.info("Subject claim: {}", claims.get(Claim.SUB));

        return KeycloakTokenAttributes.builder()
                .sub(Optional.ofNullable((String) claims.get(Claim.SUB))
                        .map(sub -> sub + "@ssb.no")
                        .orElse("@ssb.no"))
                .keycloakClientId((String) claims.get(Claim.CLIENT_ID))
                .maskinportenClientId((String) claims.get(Claim.MASKINPORTEN_CLIENT_ID))
                .maskinportenAudience((String) claims.get(Claim.MASKINPORTEN_AUDIENCE))
                .skyportenAudience((String) claims.get(Claim.SKYPORTEN_AUDIENCE))
                .maskinportenDefaultScopes(new HashSet<>((List<String>) claims.getOrDefault(Claim.MASKINPORTEN_DEFAULT_SCOPES, Collections.EMPTY_LIST)))
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

    public Optional<String> getSkyportenAudience() {
        return Optional.ofNullable(skyportenAudience);
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
        static final String SKYPORTEN_AUDIENCE = "skyporten_audience";
        static final String SUB = "sub";
        static final String CLIENT_ID = "clientId";
    }

}
