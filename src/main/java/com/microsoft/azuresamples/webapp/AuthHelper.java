package com.microsoft.azuresamples.webapp;

import com.microsoft.aad.msal4j.*;
import com.microsoft.azuresamples.webapp.authentication.MsalAuthSession;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthHelper {
    private static ConfidentialClientApplication confClientInstance;
    private static final String AUTHORITY = Config.getProperty("aad.authority");
    private static final String CLIENT_ID = Config.getProperty("aad.clientId");
    private static final String SECRET = Config.getProperty("aad.secret");
    private static final String SCOPES = Config.getProperty("aad.scopes");
    private static final Long STATE_TTL = Long.parseLong(Config.getProperty("app.stateTTL"));
    private static final String REDIRECT_URI = Config.getProperty("app.redirectUri");

    public static ConfidentialClientApplication getConfidentialClientInstance() {
        if (confClientInstance == null)
            return instantiateConfidentialClient();
        return confClientInstance;
    }

    public static void doAuthorizationRequest(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();

        final MsalAuthSession msalAuthSession = getMsalAuthSession(req.getSession());
        msalAuthSession.setStateAndNonce(state, nonce);

        resp.setStatus(302);
        final String redirectUrl = getAuthorizationRequestUrl(SCOPES, state, nonce);
        resp.sendRedirect(redirectUrl);
    }

    public static IAuthenticationResult processAuthCodeRedirect(final HttpServletRequest req) throws Exception {
        final String nonce = req.getParameter("nonce");
        final String state = req.getParameter("state");
        final String authCode = req.getParameter("code");

        if (authCode != null && validateNonceAndState(req.getSession(), nonce, state)) {
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters.builder(
                    authCode,
                    new URI(req.getRequestURI())
                    ).build();

                    final Future<IAuthenticationResult> future = 
                        AuthHelper.getConfidentialClientInstance().acquireToken(authParams);
                    IAuthenticationResult result = future.get();
                    if (result == null) {
                        System.out.println("authentication result was null");
                    }
                    return result;
        }
        System.out.println("Couldn't process AuthCode");
        return null;

    }

    private static boolean validateNonceAndState(final HttpSession session, final String nonce, final String state) {
        final MsalAuthSession msalAuth = getMsalAuthSession(session);
        final String savedState = msalAuth.getState();
        final String savedNonce = msalAuth.getNonce();

        final Date now = new Date();
        if (savedNonce == null || savedState == null || nonce == null || state == null || !savedNonce.equals(nonce)
                || !savedState.equals(state)
                || msalAuth.getStateDate().before(new Date(now.getTime() - (STATE_TTL * 1000)))) {
            System.out.println("Nonce/State mismatch or null or empty on validateNonceAndState");
            return false;
        }
        return true;
    }

    public static MsalAuthSession getMsalAuthSession(final HttpSession session) {
        return MsalAuthSession.getMsalAuthSession(session);
    }

    private static String getAuthorizationRequestUrl(final String scopes, final String state, final String nonce) {
        final AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(REDIRECT_URI, Collections.singleton(scopes)).responseMode(ResponseMode.QUERY)
                .prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).build();

        return getConfidentialClientInstance().getAuthorizationRequestUrl(parameters).toString();
    }

    private static ConfidentialClientApplication instantiateConfidentialClient() {
        try {
            confClientInstance = ConfidentialClientApplication
                    .builder(CLIENT_ID, ClientCredentialFactory.createFromSecret(SECRET)).b2cAuthority(AUTHORITY)
                    .build();
        } catch (final MalformedURLException ex) {
            System.out.println("Failed to create Confidential Client Application");
        }
        return confClientInstance;
    }


}
