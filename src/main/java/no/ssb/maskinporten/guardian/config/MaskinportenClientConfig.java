package no.ssb.maskinporten.guardian.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@EachProperty(value = "maskinporten.clients")
public class MaskinportenClientConfig {

    /** clientId is the maskinporten client ID from samarbeidsportalen, typically a UUID */
    private String clientId;

    /** description is an optional string that describes some contextual information about the maskinporten client. Only used for documentation purposes. */
    private String description;

    /** audience is the intended target for the JWT grant, ie. the identifier for Maskinporten. *** MUST end with a slash (/) *** */
    private String audience;

    /** numberOfSecondsLeftBeforeExpire is the max number of seconds that can span before acquired access tokens must be used. Defaults to 10. */
    private int numberOfSecondsLeftBeforeExpire = 10;

    /** authorizedUsers holds a set of users that are authorized to retrieve access tokens on behalf of this maskinporten client. */
    private Set<String> authorizedUsers = new HashSet<>();

    public MaskinportenClientConfig(@Parameter String clientId) {
        this.clientId = clientId;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String clientId) {
            super("No maskinporten client configuration found for clientId=" + clientId +
              ". Make sure the application.yml contains a config section for this client.");
        }
    }

    /**
     * return the endpoint url at the maskinporten API that issues access tokens from JWT grants.
     * See: https://docs.digdir.no/maskinporten_protocol_token.html
     */
    public String getTokenEndpoint() {
        return audience + "token";
    }

}
