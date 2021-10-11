package no.ssb.maskinporten.guardian;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ssb.maskinporten.guardian.config.MaskinportenClientConfig;
import no.ssb.maskinporten.guardian.util.PrincipalUtil;

import java.security.Principal;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AccessTokenController {

    private final MaskinportenService maskinportenService;
    private final ClientAuthorizer clientAuthorizer;
    private final MeterRegistry meterRegistry;

    @Post("/access-token")
    public HttpResponse<AccessTokenResponse> fetchMaskinportenAccessToken(Principal principal, AccessTokenRequest request) {
        log.info("Request: {}", request);
        log.info("AUDIT {}", PrincipalUtil.auditInfoOf(principal));
        meterRegistry.counter("guardian.access", "request", "token-fetch-attempt").increment();
        clientAuthorizer.validateMaskinportenClientUsageAuthorization(request.getMaskinportenClientId(), principal);
        String maskinportenAccessToken = maskinportenService.getAccessToken(request);
        meterRegistry.counter("guardian.access", "request", "token-fetch-success").increment();
        return HttpResponse.ok(AccessTokenResponse.builder()
          .accessToken(maskinportenAccessToken)
          .build()
        );
    }

    @Error
    public HttpResponse<JsonError> maskinportenClientTokenRequestError(HttpRequest request, no.ks.fiks.maskinporten.error.MaskinportenClientTokenRequestException e) {
        meterRegistry.counter("guardian.error", "error", "token-request-error").increment();
        return error(request, e, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage() + " - " + e.getMaskinportenError());
    }

    @Error
    public HttpResponse<JsonError> clientUsageNotAuthorizedError(HttpRequest request, ClientAuthorizer.NotAuthorizedForMaskinportenClientUsageException e) {
        meterRegistry.counter("guardian.error", "error", "unauthorized-client-usage").increment();
        return error(request, e, HttpStatus.FORBIDDEN, e.getMessage());
    }

    @Error
    public HttpResponse<JsonError> maskinportenClientNotFoundError(HttpRequest request, MaskinportenClientConfig.NotFoundException e) {
        meterRegistry.counter("guardian.error", "error", "maskinporten-client-config-not-found").increment();
        return error(request, e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    static HttpResponse<JsonError> error(HttpRequest request, Exception e, HttpStatus httpStatus, String httpStatusReason) {
        JsonError error = new JsonError(e.getMessage())
          .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>status(httpStatus, httpStatusReason)
          .body(error);
    }

    @Data
    public static class AccessTokenRequest {
        private String maskinportenClientId;
        private Set<String> scopes;
    }

    @Data
    @Builder
    static class AccessTokenResponse {
        private String accessToken;
    }

}
