package no.ssb.maskinporten.guardian.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("certificates")
@Data
@Context
@Valid
public class CertificateConfig {

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
