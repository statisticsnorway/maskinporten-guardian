package no.ssb.guardian;

import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.retry.event.RetryEvent;
import io.micronaut.retry.event.RetryEventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RetryListener implements RetryEventListener {
    @Override
    public void onApplicationEvent(RetryEvent event) {
        final MutableArgumentValue<?> request = event.getSource().getParameters().get("request");
        log.error("Request failed {} time(s) for {}:", event.getRetryState().currentAttempt(),
                request != null ? request.getValue(): "<empty request>",
                event.getThrowable());
    }

    @Override
    public boolean supports(RetryEvent event) {
        return true;
    }
}
