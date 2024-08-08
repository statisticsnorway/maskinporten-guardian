package no.ssb.guardian.core.cert;

import lombok.Value;

import java.util.Date;

@Value
public class CertificateValidity {
    enum Status {
        OK, WARN, ERROR, FATAL
    }
    Status status;
    Date expiryDate;
    String message;
}
