package no.ssb.maskinporten.guardian;

/**
 * MaskinportenGuardianUserType denotes different types of consumers of the Maskinporten Guardian API
 *
 * This information is used to determine different levels of security measures and logging requirements. It will
 * typically be deduced from the consumer's keycloak token.
 */
public enum MaskinportenGuardianUserType {
    /**
     * Service account user tailor made for accessing Maskinporten on behalf of a specific maskinporten client id
     */
    MASKINPORTEN_GUARDIAN_SERVICE_ACCOUNT,

    /**
     * Personal user account
     */
    PERSON;
}
