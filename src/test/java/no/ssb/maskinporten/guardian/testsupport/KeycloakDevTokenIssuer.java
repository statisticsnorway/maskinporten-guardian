package no.ssb.maskinporten.guardian.testsupport;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class KeycloakDevTokenIssuer {

    private KeycloakDevTokenIssuer() {}

    public static final String SECRET_BASE64 = "b3BlbnNlc2FtZS1zaW0tc2FsYS1iYWxhLWJpbS1iYWxhLWJvbQ==";

    private static final String DEFAULT_ISSUER = "https://mock-keycloak.dev-bip-app.ssb.no/auth/realms/ssb";
    private static final int TEN_DAYS = 864_000;

    public static String maskinportenServiceAccountAccessToken(String maskinportenClientId, List<String> maskinportenDefaultScopes) {
        Map<String, Object> claims = new HashMap<>();
        String username = "maskinporten-" + maskinportenClientId;

        claims.put(Claim.AUDIENCE, List.of("maskinporten-guardian", "account"));
        claims.put(Claim.AUTH_CONTENT_CLASS_REFERENCE, "1");
        claims.put(Claim.AUTHORIZED_PARTY, username);
        claims.put(Claim.CLIENT_ID, username);
        claims.put(Claim.EMAIL_VERIFIED, false);
        claims.put(Claim.MASKINPORTEN_CLIENT_ID, maskinportenClientId);
        claims.put(Claim.MASKINPORTEN_AUDIENCE, "https://ver2.maskinporten.no/");
        claims.put(Claim.MASKINPORTEN_DEFAULT_SCOPES, maskinportenDefaultScopes);
        claims.put(Claim.PREFERRED_USERNAME, "service-account-" + username);
        claims.put(Claim.SCOPE, "email profile");

        return TokenGenerator.createToken(claims);
    }

    public static String jupyterPersonalAccessToken(String firstName, String lastName) {
        String email = "%s.%s@ssb.no".formatted(firstName, lastName).toLowerCase();

        Map<String, Object> claims = new HashMap<>();
        claims.put(Claim.AUDIENCE, List.of("jupyter", "account"));
        claims.put(Claim.AUTH_CONTENT_CLASS_REFERENCE, "0");
        claims.put(Claim.AUTH_TIME, nowInSeconds());
        claims.put(Claim.AUTHORIZED_PARTY, "jupyter");
        claims.put(Claim.EMAIL, email);
        claims.put(Claim.EMAIL_VERIFIED, true);
        claims.put(Claim.FAMILY_NAME, lastName);
        claims.put(Claim.GIVEN_NAME, firstName);
        claims.put(Claim.PREFERRED_USERNAME, email);
        claims.put(Claim.NAME, "%s, %s".formatted(lastName, firstName));
        claims.put(Claim.SCOPE, "openid email profile");

        return TokenGenerator.createToken(claims);
    }

    private static Long expiryTime(int secondsFromNow) {
        return nowInSeconds() + secondsFromNow;
    }

    private static Long nowInSeconds() {
        return System.currentTimeMillis() / 1000;
    }


    /**
     * Util for generating HS256 signed JWT tokens for testing
     */
    static class TokenGenerator {

        private TokenGenerator() {}

        public static String createToken(final Map<String, Object> claims) {
            return getBuilder(claims).compact();
        }

        static JwtBuilder getBuilder(final Map<String, Object> claims) {
            if (!claims.containsKey(Claim.ISSUER)) {
                claims.put(Claim.ISSUER, DEFAULT_ISSUER);
            }
            if (!claims.containsKey(Claim.EXPIRY)) {
                claims.put(Claim.EXPIRY, expiryTime(TEN_DAYS));
            }

            claims.put(Claim.TYPE, "Bearer");
            claims.put(Claim.ISSUED_AT, nowInSeconds());

            // !!! The order of the setClaims invocation is important !!!
            return Jwts.builder()
              .setHeaderParam("typ", "JWT")
              .signWith(SignatureAlgorithm.HS256, SECRET_BASE64)
              .setClaims(claims)
              .setSubject(UUID.randomUUID().toString())
              .setId(UUID.randomUUID().toString())
              ;
        }
    }

    /**
     * Claim names
     */
    public static class Claim {
        // standard claims
        public static final String AUDIENCE = "aud";
        public static final String AUTH_CONTENT_CLASS_REFERENCE = "acr";
        public static final String AUTH_TIME = "auth_time";
        public static final String AUTHORIZED_PARTY = "azp";
        public static final String EMAIL = "email";
        public static final String EMAIL_VERIFIED = "email_verified";
        public static final String EXPIRY = "exp";
        public static final String FAMILY_NAME = "family_name";
        public static final String GIVEN_NAME = "given_name";
        public static final String ISSUED_AT = "iat";
        public static final String ISSUER = "iss";
        public static final String NAME = "name";
        public static final String PREFERRED_USERNAME = "preferred_username";
        public static final String SCOPE = "scope";
        public static final String TYPE = "typ";

        // custom claims
        public static final String CLIENT_ID = "clientId";
        public static final String MASKINPORTEN_CLIENT_ID = "maskinporten_client_id";
        public static final String MASKINPORTEN_DEFAULT_SCOPES = "maskinporten_default_scopes";
        public static final String MASKINPORTEN_AUDIENCE = "maskinporten_audience";
    }

}
