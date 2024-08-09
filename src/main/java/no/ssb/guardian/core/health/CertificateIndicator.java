package no.ssb.guardian.core.health;

import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import io.micronaut.management.health.indicator.annotation.Readiness;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ssb.guardian.core.cert.CertificateService;
import no.ssb.guardian.core.cert.CertificateStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@Readiness
@Slf4j
@RequiredArgsConstructor
/**
 * Checks the validity of the certificate used for signing requests to Maskinporten.
 * <p>
 * Uses a cached result to avoid checking the certificate validity too often.
 */
public class CertificateIndicator extends AbstractHealthIndicator<CertificateStatus> {

    private static final long CACHE_DURATION_HOURS = 2;
    private final AtomicReference<CertificateStatus> cachedStatus = new AtomicReference<>();
    private Instant lastChecked = Instant.now();

    private final CertificateService certificateService;

    @Override
    protected String getName() {
        return "certificate";
    }

    @Override
    protected CertificateStatus getHealthInformation() {
        if (cachedStatus.get() != null && Instant.now().isBefore(lastChecked.plus(CACHE_DURATION_HOURS, ChronoUnit.HOURS))) {
            log.trace("Using cached certificate validity result");
            return cachedStatus.get();
        }

        log.trace("Checking certificate validity");
        cachedStatus.set(certificateService.validateCertificate());
        lastChecked = Instant.now();
        return cachedStatus.get();
    }

}
