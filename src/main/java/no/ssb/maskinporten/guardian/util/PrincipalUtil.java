package no.ssb.maskinporten.guardian.util;

import io.micronaut.security.authentication.ServerAuthentication;
import lombok.experimental.UtilityClass;

import java.security.Principal;

@UtilityClass
public class PrincipalUtil {

    /**
     * Extract audit information for a Principal
     */
    public static String auditInfoOf(Principal principal) {
        if (principal instanceof ServerAuthentication) {
            return ((ServerAuthentication) principal).getAttributes().toString();
        }
        else {
            return principal.getName();
        }
    }

}
