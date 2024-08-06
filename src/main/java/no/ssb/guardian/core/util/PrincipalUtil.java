package no.ssb.guardian.core.util;

import io.micronaut.security.authentication.ServerAuthentication;
import lombok.experimental.UtilityClass;

import java.security.Principal;
import java.util.Map;

@UtilityClass
public class PrincipalUtil {

    /**
     * Extract audit information for a Principal
     */
    public static String auditInfoOf(Principal principal) {
        if (principal instanceof ServerAuthentication) {
            Map<String, Object> attr = ((ServerAuthentication) principal).getAttributes();
            String accessType = attr.containsKey("maskinporten_client_id") ? "M2M" : "PERSONAL";
            String user = attr.getOrDefault("preferred_username", principal.getName()).toString();
            return "%s (%s) - Details: %s".formatted(accessType, user, attr);
        }
        else {
            return principal.getName();
        }
    }

}
