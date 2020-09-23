package com.microsoft.azuresamples.webapp;

import com.microsoft.aad.msal4j.*;
import com.microsoft.azuresamples.webapp.authentication.MsalAuthSession;

import java.util.logging.Level;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import java.util.UUID;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthHelper {
    private static final String AUTHORITY = Config.getProperty("aad.authority");
    private static final String CLIENT_ID = Config.getProperty("aad.clientId");
    private static final String SECRET = Config.getProperty("aad.secret");
    private static final String SCOPES = Config.getProperty("aad.scopes");
    private static final Long STATE_TTL = Long.parseLong(Config.getProperty("app.stateTTL"));
    private static final String REDIRECT_URI = Config.getProperty("app.redirectUri");
    private static final String HOME_PAGE = Config.getProperty("app.homePage");
    private static final String SIGN_OUT = Config.getProperty("aad.signOut");
    

    public static ConfidentialClientApplication getConfidentialClientInstance(String serializedTokenCache) throws Exception{
        ConfidentialClientApplication confClientInstance = null;
        Config.logger.log(Level.INFO, "Getting confidential client instance");
        try {
            IClientSecret secret = ClientCredentialFactory.createFromSecret(SECRET);
            confClientInstance = ConfidentialClientApplication
                    .builder(CLIENT_ID, secret)
                    .b2cAuthority(AUTHORITY)
                    .build();
            confClientInstance.tokenCache().deserialize(serializedTokenCache);
        } catch (final Exception ex) {
            Config.logger.log(Level.SEVERE, "Failed to create Confidential Client Application.");
            Config.logger.log(Level.SEVERE, ex.getMessage());
            Config.logger.log(Level.SEVERE, Arrays.toString(ex.getStackTrace()));
            throw ex;
        }
        return confClientInstance;
    }

    public static void redirectToSignoutEndpoint(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        req.getSession().invalidate();
        resp.sendRedirect(SIGN_OUT + "?post_logout_redirect_uri=" + URLEncoder.encode(HOME_PAGE, "UTF-8"));
    }

    public static void redirectToAuthorizationEndpoint(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();

        final MsalAuthSession msalAuthSession = getMsalAuthSession(req.getSession());
        msalAuthSession.setStateAndNonce(state, nonce);

        ConfidentialClientApplication client = getConfidentialClientInstance(msalAuthSession.getTokenCache());

        final AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(REDIRECT_URI, Collections.singleton(SCOPES)).responseMode(ResponseMode.QUERY)
                .prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).build();

        final String redirectUrl = client.getAuthorizationRequestUrl(parameters).toString();

        resp.setStatus(302);
        resp.sendRedirect(redirectUrl);
    }

    public static IAuthenticationResult processAuthCodeRedirect(final HttpServletRequest req) throws Exception {
        final String state = req.getParameter("state");
        final String authCode = req.getParameter("code");
        MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        final ConfidentialClientApplication client = AuthHelper.getConfidentialClientInstance(msalAuth.getTokenCache());
        IAuthenticationResult result = null;

        if (authCode != null && validateState(req.getSession(), state)) {
            Config.logger.log(Level.FINE, "Received AuthCode. Code is {0}", authCode);
            try {
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters
                .builder(authCode, new URI(req.getRequestURL().toString()))
                .scopes(Collections.singleton(SCOPES))
                .build();

            
            Future<IAuthenticationResult> future = client.acquireToken(authParams);
            result = future.get();
            } catch (Exception ex) {
                Config.logger.log(Level.WARNING, "Unable to exchange auth code for token");
                Config.logger.log(Level.WARNING, ex.getMessage());
                Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            }
            if (result == null || !AuthHelper.validateNonce(req.getSession(), "placeholder: nonce obtained from token goes here")) {
                Config.logger.log(Level.WARNING, "Acquire token by auth code was null");
                msalAuth.setAuthenticated(false);
                msalAuth.setUsername(null);
                
            } else {
                Config.logger.log(Level.INFO, "Acquire token by auth code was successful");
                msalAuth.setTokenCache(client.tokenCache().serialize());
                msalAuth.setAuthenticated(true);
                msalAuth.setUsername(result.account().username());
            }
            return result;
        }
        Config.logger.log(Level.WARNING, "Failed to process AuthCode");
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
            
            Config.logger.log(Level.WARNING, "State mismatch or null or empty on validateState");
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
}
