package no.ssb.guardian.maskinporten;

import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.guardian.maskinporten.client.MaskinportenClient;
import no.ssb.guardian.maskinporten.client.MaskinportenClientRegistry;
import no.ssb.guardian.maskinporten.config.MaskinportenClientConfig;
import no.ssb.guardian.core.metrics.MetricsService;

import java.util.Set;

@Singleton
@RequiredArgsConstructor
public class MaskinportenService {

    @NonNull
    private final MaskinportenClientRegistry maskinportenClientRegistry;

    @NonNull
    private final ClientAuthorizer clientAuthorizer;

    @NonNull
    private final MetricsService metrics;

    public String getMaskinportenAccessToken(GetMaskinportenAccessTokenDto dto) {
        if (dto.getUserType() != MaskinportenGuardianUserType.MASKINPORTEN_GUARDIAN_SERVICE_ACCOUNT) {
            clientAuthorizer.validateMaskinportenClientUsageAuthorization(dto.getClientConfig(), dto.getPrincipalName());
        }

        if (dto.getUserType() == MaskinportenGuardianUserType.PERSON) {
            metrics.incrementRequestAccess("token-fetch-personal-behalf");
        }

        MaskinportenClient maskinportenClient = maskinportenClientRegistry.get(dto.getClientConfig());
        String maskinportenAccessToken = maskinportenClient.getAccessToken(dto.getRequestedScopes());
        return maskinportenAccessToken;
    }

    @Builder
    @Value
    public static class GetMaskinportenAccessTokenDto {
        @NonNull
        private String principalName;

        @NonNull
        private MaskinportenGuardianUserType userType;

        @NonNull
        private MaskinportenClientConfig clientConfig;

        @NonNull
        private Set<String> requestedScopes;
    }

}
