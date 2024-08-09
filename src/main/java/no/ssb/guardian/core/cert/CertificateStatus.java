package no.ssb.guardian.core.cert;

import lombok.Value;

import java.util.Date;

@Value
public class CertificateStatus {
    enum Condition {
        OK, WARN, ERROR, FATAL
    }
    Condition condition;
    Date expiryDate;
    String message;
}
