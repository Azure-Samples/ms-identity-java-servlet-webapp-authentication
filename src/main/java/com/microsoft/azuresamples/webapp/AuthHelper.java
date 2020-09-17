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

    public static ConfidentialClientApplication getConfidentialClientInstance(String serializedTokenCache) {
        if (confClientInstance == null)
            return instantiateConfidentialClient();
        confClientInstance.tokenCache().deserialize(serializedTokenCache);
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
        final String state = req.getParameter("state");
        final String authCode = req.getParameter("code");
        MsalAuthSession msalAuth = MsalAuthSession.getMsalAuthSession(req.getSession());
        final ConfidentialClientApplication client = AuthHelper.getConfidentialClientInstance(msalAuth.getTokenCache());
        IAuthenticationResult result = null;

        if (authCode != null && validateState(req.getSession(), state)) {
            System.out.println("auth code is " + authCode);
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters.builder(
                    authCode,
                    new URI(req.getRequestURL().toString())
                    ).build();

                    final Future<IAuthenticationResult> future = 
                        client.acquireToken(authParams);
                    result = future.get();
                    if (result == null) {
                        System.out.println("AuthHelper: acquire token result was null");
                        msalAuth.setAuthenticated(false);
                        msalAuth.setUsername(null);
                        
                    } else {
                        msalAuth.setTokenCache(client.tokenCache().serialize());
                        msalAuth.setAuthenticated(true);
                        msalAuth.setUsername(result.account().username());
                    }
                    return result;
        }
        System.out.println("AuthHelper: Couldn't process AuthCode");
        return result;

    }

    private static boolean validateState(final HttpSession session, final String state) {
        final MsalAuthSession msalAuth = getMsalAuthSession(session);
        final String savedState = msalAuth.getState();

        final Date now = new Date();
        if (savedState == null
                || state == null 
                || !savedState.equals(state)
                || msalAuth.getStateDate().before(new Date(now.getTime() - (STATE_TTL * 1000)))) {
            
            System.out.println("State mismatch or null or empty on validateState");
            return false;
        }
        msalAuth.setState(null);
        return true;
    }

    private static boolean validateNonce(final HttpSession session, final String nonce){
        // this is a stub
        // the nonce should be validated in implicit flow (public client application)
        // or in the case your service consumes the ID Token or Auth Token from
        // an untrusted source (front-end browser app, other services)
        return true;
    }

    public static MsalAuthSession getMsalAuthSession(final HttpSession session) {
        return MsalAuthSession.getMsalAuthSession(session);
    }

    private static String getAuthorizationRequestUrl(final String scopes, final String state, final String nonce) {
        final AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(REDIRECT_URI, Collections.singleton(scopes)).responseMode(ResponseMode.QUERY)
                .scopes(Collections.singleton(scopes))
                .prompt(Prompt.SELECT_ACCOUNT).state(state).build();

        return getConfidentialClientInstance("").getAuthorizationRequestUrl(parameters).toString();
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
