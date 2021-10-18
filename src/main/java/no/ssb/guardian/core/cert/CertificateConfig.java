package no.ssb.guardian.core.cert;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EachProperty(value = "certificates")
@Data
@Context
@Valid
public class CertificateConfig {

    public CertificateConfig(@Parameter String name) {
        this.name = name;
    }

    /**
     * name is the display name of the certificate config, only used for display and reference issues
     */
    @NotNull
    private String name;

    /**
     * certificateSecretId is the Secret Manager key that holds the p12 certificate for signing maskinporten JWT grants.
     */
    @NotNull
    private String certificateSecretId;

    /**
     * certificatePassphraseSecretId is the Secret Manager key that holds the passphrase for the certificate for signing maskinporten JWT grants.
     */
    @NotNull
    private String certificatePassphraseSecretId;

    /**
     * certificateKeystoreEntryAlias is the alias name for the entry of the keystore
     */
    private String certificateKeystoreEntryAlias;

}
