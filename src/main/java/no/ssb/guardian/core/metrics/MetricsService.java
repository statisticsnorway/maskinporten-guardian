package no.ssb.guardian.core.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

// TODO: How do we associate the maskinportenClientId with the metrics?

@Singleton
@RequiredArgsConstructor
public class MetricsService {

    // TODO: Initialize our custom metrics (set counter to 0)?
    private final MeterRegistry meterRegistry;

    public MeterRegistry meterRegistry() {
        return meterRegistry;
    }

    public void incrementRequestAccess(String tag) {
        incrementCounter("guardian.access", "request", tag);
    }

    public void incrementSecurityError(String tag) {
        incrementCounter("guardian.error", "security-error", tag);
    }

    public void incrementServerError(String tag) {
        incrementCounter("guardian.error", "server-error", tag);
    }

    public void incrementClientError(String tag) {
        incrementCounter("guardian.error", "client-error", tag);
    }

    private void incrementCounter(String name, String baseTag, String tag) {
        meterRegistry.counter(name, baseTag, tag).increment();
    }

}
