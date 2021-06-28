package com.microsoft.azuresamples.msal4j.helpers;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;

public class JwtVerifier {
    final Logger logger = Logger.getLogger(JwtVerifier.class.getName());

    String azurePublicKeyUrl;
    String issuer;
    String audience;
    String scopes;
    JWSKeySelector<SecurityContext> jwsKeySelector;
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    /**
     * Prepare the JwtVerifier
     * @param azurePublicKeyUrl
     * @param issuer
     * @param audience
     * @throws MalformedURLException
     */
    public JwtVerifier(String azurePublicKeyUrl, String issuer, String audience, String scopes) throws MalformedURLException {

        this.azurePublicKeyUrl = azurePublicKeyUrl;
        this.issuer = issuer;
        this.audience = audience;
        this.scopes = scopes;

        // Configure the JWT processor with a key selector to feed matching public
        // RSA keys sourced from the JWK set URL
        try {
            jwsKeySelector = getJWSKeySelector(azurePublicKeyUrl);
        } catch(MalformedURLException e) {
            logger.log(Level.SEVERE, "FATAL: Could not initialize JwtVerifier due to malformed URL - check your azurePublicKeyUrl param!");
            throw e;
        }
        // Create a JWT processor for the access tokens
        jwtProcessor = new DefaultJWTProcessor<>();

        // Set the required "typ" header "at+jwt" for access tokens issued by the
        // Connect2id server, may not be set by other servers
        // jwtProcessor.setJWSTypeVerifier(
        //     //new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt"))); //

        // The public RSA keys to validate the signatures will be sourced from the
        // OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
        // object caches the retrieved keys to speed up subsequent look-ups and can
        // also handle key-rollover
        jwtProcessor.setJWSKeySelector(jwsKeySelector);

        // Set the exact match and required JWT claims
        // normally we would pass .issuer("https://aad_address/tenant_id") to the builder, 
        //      but we have multiple aliases at AAD so we will (for now) just make sure 
        //      that the iss claim is present and then verify its value is acceptable afterwards.
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder().issuer(issuer).audience(audience).build(),
            new HashSet<>(Arrays.asList("sub", "iat", "exp", "scp", "iss")
            )));
    }

    public JWTClaimsSet getJWTClaimsSet(String accessToken) throws ParseException, BadJOSEException, JOSEException {
        SecurityContext ctx = null; // optional context parameter, not required here
        // Process the token (including verification)
        // return the claims set
        return jwtProcessor.process(accessToken, ctx);
    }
    /**
     * AAD has many aliases. We need to account for this when verifying 
     * @return
     */

    private void issuerVerifier() {

    }

    private JWSKeySelector<SecurityContext> getJWSKeySelector(String azurePublicKeyUrl) throws MalformedURLException {
        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(azurePublicKeyUrl));

        // The expected JWS algorithm of the access tokens (agreed out-of-band)
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        // Configure the JWT processor with a key selector to feed matching public
        // RSA keys sourced from the JWK set URL
        return new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
    }

}
