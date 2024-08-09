package no.ssb.guardian.core.cert;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    @Named("ssb-maskinporten-virksomhetssertifikat")
    private final CertificateConfig certificateConfig;

    private final KeyStoreService keyStoreService;

    public CertificateWrapper loadCertificate() {
        KeyStoreService.KeyStoreWrapper ks = loadKeyStore();
        try {
            return CertificateWrapper.builder()
                    .privateKey((PrivateKey) ks.getKeyStore().getKey(ks.getAlias(), ks.getKeyStorePassword()))
                    .certificate((X509Certificate) ks.getKeyStore().getCertificate(ks.getAlias()))
                    .build();
        }
        catch (Exception e) {
            throw new CertificateLoadException("Error loading certificate", e);
        }
    }

    KeyStoreService.KeyStoreWrapper loadKeyStore() {
        return keyStoreService.load(certificateConfig.getCertificateSecretId(), certificateConfig.getCertificatePassphraseSecretId(), certificateConfig.getCertificateKeystoreEntryAlias());
    }

    public CertificateStatus validateCertificate() {
        CertificateStatus validity = getCertificateStatus();
        return validateCertificate(validity);
    }

    public CertificateStatus validateCertificate(X509Certificate certificate) {
        CertificateStatus validity = getCertificateStatus(certificate);
        return validateCertificate(validity);
    }

    CertificateStatus validateCertificate(CertificateStatus validity) {
        switch (validity.getCondition()) {
            case OK:
                log.trace("Certificate is OK, expiry date: {}", validity.getExpiryDate());
                break;

            case WARN:
                log.warn(validity.getMessage());
                break;

            case ERROR:
                log.error(validity.getMessage());
                break;

            case FATAL:
                log.error(validity.getMessage());
                throw new CertificateExpiredException(validity.getMessage(), validity.getExpiryDate());
        }

        return validity;
    }

    CertificateStatus getCertificateStatus() {
        return getCertificateStatus(loadCertificate().getCertificate());
    }

    CertificateStatus getCertificateStatus(X509Certificate cert) {
        Date expiryDate = cert.getNotAfter();
        LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        if (expiryLocalDate.isBefore(now)) {
            return new CertificateStatus(CertificateStatus.Condition.FATAL, expiryDate,
                    "!!!ACTION REQUIRED!!! The certificate used for signing Maskinporten requests (SSB Virksomhetssertifikat) is expired (%s)."
                            .formatted(expiryDate));
        }
        else if (expiryLocalDate.isBefore(now.plusMonths(1))) {
            return new CertificateStatus(CertificateStatus.Condition.ERROR, expiryDate,
                    "!!!ACTION REQUIRED!!! The certificate used for signing Maskinporten requests (SSB Virksomhetssertifikat) is about to expire (%s)."
                            .formatted(expiryDate));
        }
        else if (expiryLocalDate.isBefore(now.plusMonths(3))) {
            return new CertificateStatus(CertificateStatus.Condition.WARN, expiryDate,
                    "The certificate used for signing Maskinporten requests (SSB Virksomhetssertifikat) expires soon (%s). It should be updated ASAP."
                            .formatted(expiryDate));
        }
        else {
            return new CertificateStatus(CertificateStatus.Condition.OK, expiryDate,"OK");
        }
    }

    @Builder
    @Value
    public static class CertificateWrapper {
        @NonNull
        private final PrivateKey privateKey;

        @NonNull
        private final X509Certificate certificate;
    }

    public static class CertificateLoadException extends RuntimeException {
        public CertificateLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CertificateExpiredException extends RuntimeException {
        public CertificateExpiredException(String message, Date expiryDate) {
            super(message);
        }
    }
}
