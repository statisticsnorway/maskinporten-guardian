package no.ssb.guardian.core.secret;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
@Requires(property = SecretServiceConfig.PREFIX + ".impl", value = "LOCAL")
public class LocalSecretService implements SecretService {

    private final SecretServiceConfig config;

    @Override
    public byte[] getSecret(String secretId) {
        return config.getOverriddenSecret(secretId)
          .orElseThrow(() -> new SecretServiceException("No local secret with id " + secretId + " found. Make sure to initialize " + SecretServiceConfig.PREFIX + ".overrideValues or " + SecretServiceConfig.PREFIX + ".overrideFiles"));
    }

    @Override
    public byte[] getSecret(String secretId, String version) {
        return getSecret(secretId);
    }

    @Override
    public byte[] getCacheableSecret(String secretId) {
        return getSecret(secretId);
    }

    @Override
    public byte[] getCacheableSecret(String secretId, String version) {
        return getSecret(secretId);
    }

}
