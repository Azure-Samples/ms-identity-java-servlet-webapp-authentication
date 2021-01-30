// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.helpers;

import com.microsoft.aad.msal4j.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * This class contains almost all of our authentication logic MSAL Java apps
 * using this sample repository's paradigm will require this.
 */
public class AuthHelper {
    private static Logger logger = Logger.getLogger(AuthHelper.class.getName());

    public static ConfidentialClientApplication getConfidentialClientInstance() throws MalformedURLException {
        ConfidentialClientApplication confClientInstance = null;
        logger.log(Level.INFO, "Getting confidential client instance");
        try {
            final IClientSecret secret = ClientCredentialFactory.createFromSecret(Config.SECRET);
            confClientInstance = ConfidentialClientApplication.builder(Config.CLIENT_ID, secret).authority(Config.AUTHORITY).build();
        } catch (final Exception ex) {
            logger.log(Level.SEVERE, "Failed to create Confidential Client Application.");
            throw ex;
        }
        return confClientInstance;
    }

    public static void signIn(IContextAdapter contextAdapter) throws AuthException, IOException {
        logger.log(Level.INFO, "sign in init");
        authorize(contextAdapter); // authorize tries to do non-interactive auth first
    }

    public static void signOut(IContextAdapter contextAdapter) throws IOException {
        logger.log(Level.INFO, "sign out init");
        redirectToSignOutEndpoint(contextAdapter);
    }

    public static void redirectToSignOutEndpoint(IContextAdapter contextAdapter) throws IOException {
        contextAdapter.getContext().clear();
        final String redirect = String.format("%s%s%s%s", Config.AUTHORITY, Config.SIGN_OUT_ENDPOINT, Config.POST_SIGN_OUT_FRAGMENT,
                URLEncoder.encode(Config.HOME_PAGE, "UTF-8"));
        contextAdapter.redirectUser(redirect);
    }

    public static void authorize(IContextAdapter contextAdapter) throws IOException, AuthException {

        final IdentityContextData context = contextAdapter.getContext();
        logger.log(Level.INFO, "preparing to authorize");
        final IAuthenticationResult authResult = context.getAuthResult();
        if (authResult != null) {
            logger.log(Level.INFO, "found auth result in session. trying to silently acquire token...");
            acquireTokenSilently(contextAdapter, authResult.account());
        } else {
            logger.log(Level.INFO,
                    "did not find auth result in session. trying to interactively acquire token...");
            redirectToAuthorizationEndpoint(contextAdapter);
        }
    }

    public static void acquireTokenSilently(IContextAdapter contextAdapter, final IAccount account) throws AuthException {
        final IdentityContextData context = contextAdapter.getContext();
        final SilentParameters parameters = SilentParameters.builder(Collections.singleton(Config.SCOPES), account).build();

        try {
            final ConfidentialClientApplication client = getConfidentialClientInstance();
            client.tokenCache().deserialize(context.getTokenCache());
            logger.log(Level.INFO, "preparing to acquire silently");
            final IAuthenticationResult result = client.acquireTokenSilently(parameters).get();
            logger.log(Level.INFO, "got auth result!");
            if (result != null) {
                logger.log(Level.INFO, "silent auth returned result. attempting to parse and process...");
                context.setAuthResult(result, client.tokenCache().serialize());
                logger.log(Level.INFO, "silent auth success!");
            } else {
                logger.log(Level.INFO, "silent auth returned null result! redirecting to authorize with code");
                throw new AuthException("Unexpected Null result when attempting to acquire token silently.");
            }
        } catch (final Exception ex) {
            String message = String.format("Failed to acquire token silently:%n %s", ex.getMessage());
            logger.log(Level.WARNING, message);
            logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
            throw new AuthException(message);
        }
    }

    private static void redirectToAuthorizationEndpoint(IContextAdapter contextAdapter) throws IOException {
        final IdentityContextData context = contextAdapter.getContext();

        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();

        context.setStateAndNonce(state, nonce);

        final ConfidentialClientApplication client = getConfidentialClientInstance();
        AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters.builder(Config.REDIRECT_URI, Collections.singleton(Config.SCOPES))
                .responseMode(ResponseMode.QUERY).prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).build();

        final String redirectUrl = client.getAuthorizationRequestUrl(parameters).toString();
        contextAdapter.redirectUser(redirectUrl);
    }

    public static void processAADCallback(IContextAdapter contextAdapter)
            throws AuthException {
        logger.log(Level.INFO, "processing redirect request...");
        final IdentityContextData context = contextAdapter.getContext();

        try {
            // FIRST, WE MUST VALIDATE THE STATE
            // ***** it is essential for CSRF protection ******
            // if no match, this throws an exception and we stop processing right here:
            validateState(contextAdapter);

            // if the state matches, continue, try to interpret any error codes.
            // e.g. redirect to pw reset. this will throw an error & cancel code x-change
            processErrorCodes(contextAdapter);

            // if no errors in request, continue to try to process auth code x-change:
            final String authCode = contextAdapter.getParameter("code");
            logger.log(Level.FINE, "request code param is {0}", authCode);
            if (authCode == null) // if no auth code, error out:
                throw new AuthException("Auth code is not in request!");

            // if auth code exists, proceed to exchange for token:
            logger.log(Level.INFO, "Received AuthCode! Processing Auth code exchange...");

            // build the auth code params:
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters
                    .builder(authCode, new URI(Config.REDIRECT_URI)).scopes(Collections.singleton(Config.SCOPES)).build();

            // Get a client instance and leverage it to acquire the token:
            final ConfidentialClientApplication client = AuthHelper.getConfidentialClientInstance();
            final IAuthenticationResult result = client.acquireToken(authParams).get();

            // parse IdToken claims from the IAuthenticationResult:
            // (the next step - validate Nonce - requires this)
            context.setIdTokenClaims(result.idToken());

            // if nonce is invalid, stop immediately! this could be a token replay!
            // if validation fails, throws exception and cancels auth:
            validateNonce(context);

            // set user to authenticated:
            context.setAuthResult(result, client.tokenCache().serialize());
        } catch (final Exception ex) {
            context.clear(); // clear the session since there was a problem
            String message = String.format("Unable to exchange auth code for token:%n %s", ex.getMessage());
            logger.log(Level.WARNING, message);
            logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
            throw new AuthException(message);
        }
    }

    private static void validateState(IContextAdapter contextAdapter) throws AuthException {
        logger.log(Level.INFO, "validating state...");

        final String requestState = contextAdapter.getParameter("state");
        final IdentityContextData context = contextAdapter.getContext();
        final String sessionState = context.getState();
        final Date now = new Date();

        logger.log(Level.FINE, "session state is: {0} \n request state param is: {1}",
                new String[] { sessionState, requestState });

        // if state is null or doesn't match or TTL expired, throw exception
        if (sessionState == null || requestState == null || !sessionState.equals(requestState)
                || context.getStateDate().before(new Date(now.getTime() - (Config.STATE_TTL * 1000)))) {
            throw new AuthException("ValidateState() indicates state param mismatch, null, empty or expired.");
        }

        logger.log(Level.INFO, "confirmed that state is valid and matches!");
        context.setState(null); // don't allow re-use of state
    }

    private static void processErrorCodes(IContextAdapter contextAdapter) throws AuthException {
        final String error = contextAdapter.getParameter("error");
        logger.log(Level.INFO, "error is {0}", error);
        final String errorDescription = contextAdapter.getParameter("error_description");
        logger.log(Level.INFO, "error description is {0}", errorDescription);
        if (error != null || errorDescription != null) {
            throw new AuthException(String.format("Received an error from AAD. Error: %s %nErrorDescription: %s", error, errorDescription));
        }
    }

    private static void validateNonce(IdentityContextData context) throws AuthException {
        logger.log(Level.INFO, "validating nonce...");

        final String nonceClaim = context.getIdTokenClaims().get("nonce");
        final String sessionNonce = context.getNonce();

        logger.log(Level.FINE, "session nonce is: {0} \n nonce claim in token is: {1}",
                new String[] { sessionNonce, nonceClaim });
        if (sessionNonce == null || !sessionNonce.equals(nonceClaim)) {
            throw new AuthException("ValidateNonce() indicates that nonce validation failed.");

        }
        logger.log(Level.INFO, "confirmed that nonce is valid and matches!");
        context.setNonce(null); // don't allow re-use of nonce
    }
}
