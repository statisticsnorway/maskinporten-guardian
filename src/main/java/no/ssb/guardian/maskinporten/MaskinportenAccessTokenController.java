package no.ssb.guardian.maskinporten;

import com.google.common.annotations.VisibleForTesting;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.retry.annotation.RetryPredicate;
import io.micronaut.retry.annotation.Retryable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import no.ssb.guardian.core.metrics.MetricsService;
import no.ssb.guardian.core.util.PrincipalUtil;
import no.ssb.guardian.maskinporten.config.MaskinportenClientConfig;
import no.ssb.guardian.maskinporten.config.MaskinportenConfig;

import java.net.SocketException;
import java.security.Principal;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@Secured(SecurityRule.IS_AUTHENTICATED)
public class MaskinportenAccessTokenController {

    private final MetricsService metrics;
    private final MaskinportenService maskinportenService;
    private final MaskinportenConfig maskinportenConfig;

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Post("/maskinporten/access-token")
    @Retryable(attempts = "5", predicate = WrappedSocketExceptionRetryPredicate.class)
    public HttpResponse<AccessTokenResponse> fetchMaskinportenAccessToken(Principal principal, @Body FetchMaskinportenAccessTokenRequest request) {
        log.debug("Request: {}", request);
        log.info("AUDIT {}", PrincipalUtil.auditInfoOf(principal));

        metrics.incrementRequestAccess("token-fetch-attempt");
        KeycloakTokenAttributes keycloakTokenAttr = KeycloakTokenAttributes.parse(principal);
        MaskinportenService.GetMaskinportenAccessTokenDto dto = buildServiceRequest(keycloakTokenAttr, request);
        String maskinportenAccessToken = maskinportenService.getMaskinportenAccessToken(dto);
        metrics.incrementRequestAccess("token-fetch-success");

        return HttpResponse.ok(AccessTokenResponse.builder()
          .accessToken(maskinportenAccessToken)
          .build()
        );
    }

    @VisibleForTesting
    MaskinportenService.GetMaskinportenAccessTokenDto buildServiceRequest(KeycloakTokenAttributes token, FetchMaskinportenAccessTokenRequest request) {
        final String maskinportenClientId = token.getMaskinportenClientId().orElse(request.getMaskinportenClientId());
        final MaskinportenClientConfig maskinportenClientConfig;

        if (token.getUserType() == MaskinportenGuardianUserType.MASKINPORTEN_GUARDIAN_SERVICE_ACCOUNT) {
            if (request.getMaskinportenClientId() != null) {
                throw new ClientRequestException(HttpStatus.BAD_REQUEST, "invalid-client-id", "maskinportenClientId cannot be explicitly specified for service account users");
            }
            maskinportenClientConfig = serviceAccountMaskinportenClientConfig(token);
        }
        else {
            if (request.getMaskinportenClientId() == null) {
                throw new ClientRequestException(HttpStatus.BAD_REQUEST, "invalid-request", "Missing required argument: maskinportenClientId");
            }
            maskinportenClientConfig = maskinportenConfig.getClientConfig(maskinportenClientId)
              .orElseThrow(() -> new MaskinportenClientConfig.NotFoundException(maskinportenClientId));
        }

        return MaskinportenService.GetMaskinportenAccessTokenDto.builder()
          .userType(token.getUserType())
          .principalName(token.getSub())
          .clientConfig(maskinportenClientConfig)
          .requestedScopes(requestedScopes(request.getScopes(), maskinportenClientConfig.getDefaultScopes()))
          .build();
    }

    @VisibleForTesting
    Set<String> requestedScopes(Set<String> requestScopes, Set<String> defaultScopes) {
        if (requestScopes != null && ! requestScopes.isEmpty()) {
            return requestScopes;
        }
        else if (defaultScopes != null && ! defaultScopes.isEmpty() ) {
            return defaultScopes;
        }
        else {
            throw new ClientRequestException(HttpStatus.BAD_REQUEST, "no-maskinporten-scopes-specified", "No maskinporten scopes specified");
        }
    }

    @VisibleForTesting
    MaskinportenClientConfig serviceAccountMaskinportenClientConfig(KeycloakTokenAttributes token) {
        if (token.getUserType() != MaskinportenGuardianUserType.MASKINPORTEN_GUARDIAN_SERVICE_ACCOUNT) {
            throw new IllegalArgumentException("Wrong user type");
        }

        String maskinportenClientId = token.getMaskinportenClientId().get();
        MaskinportenClientConfig config = new MaskinportenClientConfig(maskinportenClientId);

        // Base the generated config on a selection of explicitly configured properties if these exist
        // in the preconfigured application config
        MaskinportenClientConfig preconfigured = maskinportenConfig.getClientConfig(maskinportenClientId).orElse(null);
        if (preconfigured != null) {
            config.setNumberOfSecondsLeftBeforeExpire(preconfigured.getNumberOfSecondsLeftBeforeExpire());
            config.setDefaultScopes(preconfigured.getDefaultScopes());
        }

        config.setDescription("Generated service account config for maskinporten client id " + maskinportenClientId);
        config.setAudience(token.getMaskinportenAudience().get());
        config.setDefaultScopes(token.getMaskinportenDefaultScopes().orElse(config.getDefaultScopes()));

        return config;
    }

    @Value
    public static class ClientRequestException extends RuntimeException {
        private final HttpStatus httpStatus;
        private final String errorTag;

        public ClientRequestException(HttpStatus httpStatus, String errorTag, String message) {
            super(message);
            this.httpStatus = httpStatus;
            this.errorTag = errorTag;
        }

    }

    /**
     * The {@link io.micronaut.retry.annotation.DefaultRetryPredicate} does not support wrapped exceptions, so, this
     * prediate checks the exception {@code cause} for a {@link SocketException}.
     */
    public static class WrappedSocketExceptionRetryPredicate implements RetryPredicate {
        @Override
        public boolean test(Throwable throwable) {
            return throwable.getCause() != null && throwable.getCause().getClass().equals(SocketException.class);
        }
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    @Serdeable
    public static class FetchMaskinportenAccessTokenRequest {
        private String maskinportenClientId;
        private Set<String> scopes;
    }

    @Data
    @Builder
    @Serdeable
    static class AccessTokenResponse {
        private String accessToken;
    }

    @Error
    public HttpResponse<JsonError> maskinportenClientTokenRequestError(HttpRequest request, no.ks.fiks.maskinporten.error.MaskinportenClientTokenRequestException e) {
        metrics.incrementServerError("token-request-error");
        return error(request, e, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage() + " - " + e.getMaskinportenError());
    }

    @Error
    public HttpResponse<JsonError> clientUsageNotAuthorizedError(HttpRequest request, ClientAuthorizer.NotAuthorizedForMaskinportenClientUsageException e) {
        metrics.incrementSecurityError("unauthorized-client-usage");
        return error(request, e, HttpStatus.FORBIDDEN, e.getMessage());
    }

    @Error
    public HttpResponse<JsonError> maskinportenClientNotFoundError(HttpRequest request, MaskinportenClientConfig.NotFoundException e) {
        metrics.incrementClientError("maskinporten-client-config-not-found");
        return error(request, e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @Error
    public HttpResponse<JsonError> clientRequestError(HttpRequest request, MaskinportenAccessTokenController.ClientRequestException e) {
        metrics.incrementClientError(e.getErrorTag());
        return error(request, e, e.getHttpStatus(), e.getMessage());
    }

    private static HttpResponse<JsonError> error(HttpRequest request, Exception e, HttpStatus httpStatus, String httpStatusReason) {
        JsonError error = new JsonError(e.getMessage())
          .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>status(httpStatus, httpStatusReason)
          .body(error);
    }

}
