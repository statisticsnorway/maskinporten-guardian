package no.ssb.guardian;

import io.micronaut.retry.event.RetryEvent;
import io.micronaut.retry.event.RetryEventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RetryListener implements RetryEventListener {
    @Override
    public void onApplicationEvent(RetryEvent event) {
        log.error("Request failed {} time(s) for {} with error:", event.getRetryState().currentAttempt(),
                event.getSource().getExecutableMethod().getName(),
                event.getThrowable());
    }

    @Override
    public boolean supports(RetryEvent event) {
        return true;
    }
}
