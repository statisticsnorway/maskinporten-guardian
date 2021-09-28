package no.ssb.maskinporten.guardian.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConfigurationProperties("maskinporten")
@Data
@Context
public class MaskinportenConfig {
    /**
     * tokenEndpoint is the endpoint url at the maskinporten API that issues access tokens from JWT grants.
     * See: https://docs.digdir.no/maskinporten_protocol_token.html
     */
    private String tokenEndpoint = "https://oidc-ver2.difi.no/idporten-oidc-provider/token";

    /** clients is a map of maskinporten client configurations */
    private final Map<String, MaskinportenClientConfig> clients;

    public MaskinportenConfig(List<MaskinportenClientConfig> clientConfigList) {
        this.clients = clientConfigList.stream()
          .collect(Collectors.toMap(
            MaskinportenClientConfig::getClientId,
            Function.identity())
          );

        if (this.clients.isEmpty()) {
            throw new IllegalStateException("No 'maskinporten.clients' configured. Check the application configuration.");
        }
    }

    public MaskinportenClientConfig getClientConfig(String clientId) {
        if (! clients.containsKey(clientId)) {
            throw new MaskinportenClientConfig.NotFoundException(clientId);
        }

        return clients.get(clientId);
    }

}
