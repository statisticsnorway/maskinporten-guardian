package no.ssb.guardian.core.cert;

import org.junit.jupiter.api.Test;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CertificateServiceTest {

    private final CertificateConfig certificateConfig = mock(CertificateConfig.class);
    private final KeyStoreService keyStoreService = mock(KeyStoreService.class);
    private final CertificateService certificateService = new CertificateService(certificateConfig, keyStoreService);

    @Test
    void loadCertificate_shouldReturnCertificateWrapper() throws Exception {
        KeyStoreService.KeyStoreWrapper keyStoreWrapper = mock(KeyStoreService.KeyStoreWrapper.class);
        KeyStore keyStore = mock(KeyStore.class);
        PrivateKey privateKey = mock(PrivateKey.class);
        X509Certificate certificate = mock(X509Certificate.class);

        when(keyStoreService.load(any(), any(), any())).thenReturn(keyStoreWrapper);
        when(keyStoreWrapper.getKeyStore()).thenReturn(keyStore);
        when(keyStore.getKey(any(), any())).thenReturn(privateKey);
        when(keyStore.getCertificate(any())).thenReturn(certificate);

        CertificateService.CertificateWrapper result = certificateService.loadCertificate();

        assertThat(result.getPrivateKey()).isEqualTo(privateKey);
        assertThat(result.getCertificate()).isEqualTo(certificate);
    }

    @Test
    void loadCertificate_problemsLoadingCert_shouldThrowCertificateLoadException() throws Exception {
        when(keyStoreService.load(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Test Exception"));

        assertThrows(CertificateService.CertificateLoadException.class, () -> certificateService.loadCertificate());
    }

    @Test
    void validateCertificate_certIsNotAboutToExpire_shouldReturnOKStatus() {
        X509Certificate certificate = mock(X509Certificate.class);
        LocalDate futureDate = LocalDate.now().plusMonths(4);
        Date expiryDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(certificate.getNotAfter()).thenReturn(expiryDate);

        CertificateStatus result = certificateService.validateCertificate(certificate);

        assertThat(result.getCondition()).isEqualTo(CertificateStatus.Condition.OK);
    }

    @Test
    void validateCertificate_certExpiresInLessThanThreeMonths_shouldReturnWarnStatus() {
        X509Certificate certificate = mock(X509Certificate.class);
        LocalDate futureDate = LocalDate.now().plusMonths(2);
        Date expiryDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(certificate.getNotAfter()).thenReturn(expiryDate);

        CertificateStatus result = certificateService.validateCertificate(certificate);

        assertThat(result.getCondition()).isEqualTo(CertificateStatus.Condition.WARN);
    }

    @Test
    void validateCertificate_certExpiresInLessThanAMonth_shouldReturnErrorStatus() {
        X509Certificate certificate = mock(X509Certificate.class);
        LocalDate futureDate = LocalDate.now().plusWeeks(3);
        Date expiryDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(certificate.getNotAfter()).thenReturn(expiryDate);

        CertificateStatus result = certificateService.validateCertificate(certificate);

        assertThat(result.getCondition()).isEqualTo(CertificateStatus.Condition.ERROR);
    }

    @Test
    void validateCertificate_certExpired_shouldThrowCertificateExpiredException() {
        X509Certificate certificate = mock(X509Certificate.class);
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Date expiryDate = Date.from(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(certificate.getNotAfter()).thenReturn(expiryDate);

        assertThrows(CertificateService.CertificateExpiredException.class, () -> certificateService.validateCertificate(certificate));
    }
}