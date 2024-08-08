package no.ssb.guardian;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.version.VersionUtils;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ssb.guardian.core.cert.CertificateService;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class StartupListener implements ApplicationEventListener<ApplicationStartupEvent> {

    private final CertificateService certificateService;

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        log.info("Micronaut version {}", VersionUtils.getMicronautVersion());
        log.info("Guardian version {}", BuildInfo.INSTANCE.getVersionAndBuildTimestamp());
        log.info("Certificate expiry date: {}", certificateService.validateCertificateExpiry().getExpiryDate());
    }

}