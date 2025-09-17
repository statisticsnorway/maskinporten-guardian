package no.ssb.guardian.testsupport;

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
        claims.put(Claim.MASKINPORTEN_CLIENT_ID, maskinportenClientId);
        claims.put(Claim.MASKINPORTEN_AUDIENCE, "https://maskinporten.no/");
        claims.put(Claim.MASKINPORTEN_DEFAULT_SCOPES, maskinportenDefaultScopes);
        claims.put(Claim.SKYPORTEN_AUDIENCE, "https://sky.maskinporten.no");
        claims.put(Claim.SUB, "service-account-" + username);


        return TokenGenerator.createToken(claims);
    }

    public static String personalAccessToken(String username) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(Claim.AUDIENCE, List.of("jupyter", "account"));
        claims.put(Claim.SUB, username);

        String token  =  TokenGenerator.createToken(claims);
        System.out.println(token);

        return token;
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
              .setSubject(claims.get(Claim.SUB).toString())
              .setId(UUID.randomUUID().toString())
              ;
        }
    }

    /**
     * Claim names
     */
    public static final class Claim {
        private Claim() {}

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
        public static final String SUB = "sub";
        public static final String SCOPE = "scope";
        public static final String TYPE = "typ";


        // custom claims
        public static final String CLIENT_ID = "clientId";
        public static final String MASKINPORTEN_CLIENT_ID = "maskinporten_client_id";
        public static final String MASKINPORTEN_DEFAULT_SCOPES = "maskinporten_default_scopes";
        public static final String MASKINPORTEN_AUDIENCE = "maskinporten_audience";
        public static final String SKYPORTAN_ISSUER = "issuer";
        public static final String SKYPORTEN_AUDIENCE = "skyportan_audience";
    }

}
