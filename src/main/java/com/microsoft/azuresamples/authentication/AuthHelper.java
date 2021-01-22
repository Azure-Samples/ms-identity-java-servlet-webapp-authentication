// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.logging.Level;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This class contains almost all of our authentication logic MSAL Java apps
 * using this sample repository's paradigm will require this.
 */
public class AuthHelper {
    static final String AUTHORITY = Config.getProperty("aad.authority");
    static final String CLIENT_ID = Config.getProperty("aad.clientId");
    static final String SECRET = Config.getProperty("aad.secret");
    static final String SCOPES = Config.getProperty("aad.scopes");
    static final String SIGN_OUT_ENDPOINT = Config.getProperty("aad.signOutEndpoint");
    static final String POST_SIGN_OUT_FRAGMENT = Config.getProperty("aad.postSignOutFragment");
    static final Long STATE_TTL = Long.parseLong(Config.getProperty("app.stateTTL"));
    static final String HOME_PAGE = Config.getProperty("app.homePage");
    static final String REDIRECT_ENDPOINT = Config.getProperty("app.redirectEndpoint");
    static final String REDIRECT_URI = String.format("%s%s", HOME_PAGE, REDIRECT_ENDPOINT);

    public static ConfidentialClientApplication getConfidentialClientInstance() throws MalformedURLException {
        ConfidentialClientApplication confClientInstance = null;
        Config.logger.log(Level.INFO, "Getting confidential client instance");
        try {
            final IClientSecret secret = ClientCredentialFactory.createFromSecret(SECRET);
            confClientInstance = ConfidentialClientApplication.builder(CLIENT_ID, secret).authority(AUTHORITY).build();
        } catch (final Exception ex) {
            Config.logger.log(Level.SEVERE, "Failed to create Confidential Client Application.");
            throw ex;
        }
        return confClientInstance;
    }

    public static MsalAuthSession getMsalAuthSession(final HttpSession session) {
        return MsalAuthSession.getMsalAuthSession(session);
    }

    public static void signIn(final HttpServletRequest req, final HttpServletResponse resp) throws AuthException,
            IOException {
        Config.logger.log(Level.INFO, "sign in init");
        AuthHelper.authorize(req, resp); // authorize tries to do non-interactive auth first
    }

    public static void signOut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Config.logger.log(Level.INFO, "sign out init");
        AuthHelper.redirectToSignoutEndpoint(req, resp);
    }

    public static void redirectToSignoutEndpoint(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        req.getSession().invalidate();
        final String redirect = String.format("%s%s%s%s", AUTHORITY, SIGN_OUT_ENDPOINT, POST_SIGN_OUT_FRAGMENT,
                URLEncoder.encode(HOME_PAGE, "UTF-8"));
        Config.logger.log(Level.INFO, "Redirecting user to {0}", redirect);
        resp.sendRedirect(redirect);
    }

    public static void authorize(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, AuthException {

        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        Config.logger.log(Level.INFO, "preparing to authorize");
        final IAuthenticationResult authResult = msalAuth.getAuthResult();
        if (authResult != null) {
            Config.logger.log(Level.INFO, "found auth result in session. trying to silently acquire token...");
            acquireTokenSilently(req, resp, authResult.account());
        } else {
            Config.logger.log(Level.INFO,
                    "did not find auth result in session. trying to interactively acquire token...");
            redirectToAuthorizationEndpoint(req, resp);
        }
    }

    private static void acquireTokenSilently(final HttpServletRequest req, final HttpServletResponse resp,
            final IAccount account) throws AuthException {
        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        final SilentParameters parameters = SilentParameters.builder(Collections.singleton(SCOPES), account).build();

        try {
            final ConfidentialClientApplication client = getConfidentialClientInstance();
            client.tokenCache().deserialize(msalAuth.getTokenCache());
            Config.logger.log(Level.INFO, "preparing to acquire silently");
            final IAuthenticationResult result = client.acquireTokenSilently(parameters).get();
            Config.logger.log(Level.INFO, "got auth result!");
            if (result != null) {
                Config.logger.log(Level.INFO, "silent auth returned result. attempting to parse and process...");
                parseJWTClaimsSetAndStoreResultInSession(msalAuth, result, client.tokenCache().serialize());
                processSuccessfulAuthentication(msalAuth);
            } else {
                Config.logger.log(Level.INFO, "silent auth returned null result! redirecting to authorize with code");
                throw new AuthException("Unexpected Null result when attempting to acquire token silently.");
            }
        } catch (final Exception ex) {
            String message = String.format("Failed to acquire token silently:%n %s", ex.getMessage());
            Config.logger.log(Level.WARNING, message);
            Config.logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
            throw new AuthException(message);
        }
    }

    private static void redirectToAuthorizationEndpoint(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();

        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        msalAuth.setStateAndNonce(state, nonce);

        final ConfidentialClientApplication client = getConfidentialClientInstance();
        AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters.builder(REDIRECT_URI, Collections.singleton(SCOPES))
                .responseMode(ResponseMode.QUERY).prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).build();

        final String redirectUrl = client.getAuthorizationRequestUrl(parameters).toString();
        Config.logger.log(Level.INFO, "Redirecting user to {0}", redirectUrl);
        resp.sendRedirect(redirectUrl);
    }

    public static void processAADCallback(final HttpServletRequest req, final HttpServletResponse resp)
            throws AuthException {
        Config.logger.log(Level.INFO, "processing redirect request...");
        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());

        try {
            // FIRST, WE MUST VALIDATE THE STATE
            // ***** it is essential for CSRF protection ******
            // if no match, this throws an exception and we stop processing right here:
            final String state = req.getParameter("state");
            validateState(msalAuth, state);

            // if the state matches, continue, try to interpret any error codes.
            // e.g. redirect to pw reset. this will throw an error & cancel code x-change
            processErrorCodes(req, resp);

            // if no errors in request, continue to try to process auth code x-change:
            final String authCode = req.getParameter("code");
            Config.logger.log(Level.FINE, "request code param is {0}", authCode);
            if (authCode == null) // if no auth code, error out:
                throw new AuthException("Auth code is not in request!");

            // if auth code exists, proceed to exchange for token:
            Config.logger.log(Level.INFO, "Received AuthCode! Processing Auth code exchange...");

            // build the auth code params:
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters
                    .builder(authCode, new URI(REDIRECT_URI)).scopes(Collections.singleton(SCOPES)).build();

            // Get a client instance and leverage it to acquire the token:
            final ConfidentialClientApplication client = AuthHelper.getConfidentialClientInstance();
            final IAuthenticationResult result = client.acquireToken(authParams).get();

            // parse IdToken claims from the IAuthenticationResult:
            parseJWTClaimsSetAndStoreResultInSession(msalAuth, result, client.tokenCache().serialize());

            // if nonce is invalid, stop immediately! this could be a token replay!
            // if validation fails, throws exception and cancels auth:
            validateNonce(msalAuth);

            // set user to authenticated:
            processSuccessfulAuthentication(msalAuth);
        } catch (final Exception ex) {
            req.getSession().invalidate(); // clear the session since there was a problem
            String message = String.format("Unable to exchange auth code for token:%n %s", ex.getMessage());
            Config.logger.log(Level.WARNING, message);
            Config.logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
            throw new AuthException(message);
        }
    }

    private static void validateState(final MsalAuthSession msalAuth, final String stateFromRequest) throws AuthException {
        Config.logger.log(Level.INFO, "validating state...");

        final String sessionState = msalAuth.getState();
        final Date now = new Date();
        Config.logger.log(Level.FINE, "session state is: {0} \n request state param is: {1}",
                new String[] { sessionState, stateFromRequest });

        // if state is null or doesn't match or TTL expired, throw exception
        if (sessionState == null || stateFromRequest == null || !sessionState.equals(stateFromRequest)
                || msalAuth.getStateDate().before(new Date(now.getTime() - (STATE_TTL * 1000)))) {
            throw new AuthException("ValidateState() indicates state param mismatch, null, empty or expired.");
        }
        Config.logger.log(Level.INFO, "confirmed that state is valid and matches!");
        msalAuth.setState(null); // don't allow re-use of state
    }

    private static void processErrorCodes(final HttpServletRequest req, final HttpServletResponse resp)
            throws AuthException {
        final String error = req.getParameter("error");
        Config.logger.log(Level.INFO, "error is {0}", error);
        final String errorDescription = req.getParameter("error_description");
        Config.logger.log(Level.INFO, "error description is {0}", errorDescription);
        if (error != null || errorDescription != null) {
            throw new AuthException(String.format("Received an error from AAD. Error: %s %nErrorDescription: %s", error, errorDescription));
        }
    }

    private static void parseJWTClaimsSetAndStoreResultInSession(final MsalAuthSession msalAuth,
            final IAuthenticationResult result, final String serializedTokenCache) throws ParseException {
        Config.logger.log(Level.INFO, "placing JWT claims set from auth result into session...");

        final SignedJWT idToken = SignedJWT.parse(result.idToken());
        final JWTClaimsSet jcs = idToken.getJWTClaimsSet();
        msalAuth.setIdTokenClaims(jcs.getClaims());

        msalAuth.setAuthResult(result);
        msalAuth.setTokenCache(serializedTokenCache);

        Config.logger.log(Level.INFO, "placed JWT claims set into session");
    }

    private static void validateNonce(final MsalAuthSession msalAuth) throws AuthException {
        Config.logger.log(Level.INFO, "validating nonce...");

        final String nonceClaim = msalAuth.getIdTokenClaims().get("nonce");
        final String sessionNonce = msalAuth.getNonce();

        Config.logger.log(Level.FINE, "session nonce is: {0} \n nonce claim in token is: {1}",
                new String[] { sessionNonce, nonceClaim });
        if (sessionNonce == null || !sessionNonce.equals(nonceClaim)) {
            throw new AuthException("ValidateNonce() indicates that nonce validation failed.");

        }
        Config.logger.log(Level.INFO, "confirmed that nonce is valid and matches!");
        msalAuth.setNonce(null); // don't allow re-use of nonce
    }

    private static void processSuccessfulAuthentication(final MsalAuthSession msalAuth) {
        Config.logger.log(Level.INFO, "processing successful auth into session");

        msalAuth.setAuthenticated(true);
        msalAuth.setUsername(msalAuth.getIdTokenClaims().get("name"));

        Config.logger.log(Level.INFO, "successfully placed auth into session");
    }
}
