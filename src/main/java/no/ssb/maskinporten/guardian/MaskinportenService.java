package no.ssb.maskinporten.guardian;

import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.metrics.MetricsService;

import java.util.Set;

@Singleton
@RequiredArgsConstructor
public class MaskinportenService {

    @NonNull
    private final MaskinportenklientRegistry maskinportenklientRegistry;

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

        Maskinportenklient maskinportenklient = maskinportenklientRegistry.get(dto.getClientConfig());
        String maskinportenAccessToken = maskinportenklient.getAccessToken(dto.getRequestedScopes());
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
