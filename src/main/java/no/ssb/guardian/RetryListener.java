package no.ssb.guardian;

import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.retry.event.RetryEvent;
import io.micronaut.retry.event.RetryEventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import no.ssb.guardian.core.util.PrincipalUtil;

import java.security.Principal;

@Slf4j
@Singleton
public class RetryListener implements RetryEventListener {
    @Override
    public void onApplicationEvent(RetryEvent event) {
        final MutableArgumentValue<?> principal = event.getSource().getParameters().get("principal");
        final MutableArgumentValue<?> request = event.getSource().getParameters().get("request");
        if (event.getRetryState().currentAttempt() < 3) {
            log.warn("Request failed {} time(s) for {} and principal {}:", event.getRetryState().currentAttempt(),
                    request != null ? request.getValue(): "<empty request>",
                    principal != null ? PrincipalUtil.auditInfoOf((Principal) principal.getValue()): "<empty principal>",
                    event.getThrowable());
        } else {
            log.error("Request failed {} time(s) for {} and principal {}:", event.getRetryState().currentAttempt(),
                    request != null ? request.getValue(): "<empty request>",
                    principal != null ? PrincipalUtil.auditInfoOf((Principal) principal.getValue()): "<empty principal>",
                    event.getThrowable());
        }
    }

    @Override
    public boolean supports(RetryEvent event) {
        return true;
    }
}
