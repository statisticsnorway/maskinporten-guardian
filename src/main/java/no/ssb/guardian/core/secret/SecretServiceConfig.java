package no.ssb.guardian.core.secret;


import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties(SecretServiceConfig.PREFIX)
public class SecretServiceConfig {

    public static final String PREFIX = "services.secrets";

    public enum Impl {
        GCP, LOCAL, MOCK;
    }

    /**
     * The SecretService implementation to use. Defaults to MOCK if not specified.
     */
    private Impl impl = Impl.MOCK;

    /**
     * A map of hardcoded secrets (value based) that will override any secret IDs. This can be handy in testing situations. Secrets
     * that are defined in this map will not be looked up in SecretManager (if impl=GCP).
     */
    Map<String, byte[]> overrideValues = new LinkedHashMap<>();

    /**
     * A map of hardcoded secrets (file based) that will override any secret IDs. This can be handy in testing situations. Secrets
     * that are defined in this map will not be looked up in SecretManager (if impl=GCP).
     */
    Map<String, String> overrideFiles = new LinkedHashMap<>();

    public Optional<byte[]> getOverriddenSecret(String secretId) {
        if (overrideValues.containsKey(secretId)) {
            return Optional.ofNullable(overrideValues.get(secretId));
        }
        else if (overrideFiles.containsKey(secretId)) {
            try {
                return Optional.ofNullable(new FileInputStream(overrideFiles.get(secretId)).readAllBytes());
            } catch (IOException e) {
                throw new SecretServiceException("Error reading secret from file " + secretId, e);
            }
        }
        else {
            return Optional.empty();
        }
    }

}
