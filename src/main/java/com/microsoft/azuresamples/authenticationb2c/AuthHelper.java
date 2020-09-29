package com.microsoft.azuresamples.authenticationb2c;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.logging.Level;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthHelper {
    static final String AUTHORITY = Config.getProperty("aad.authority");
    static final String SIGN_IN_POLICY = Config.getProperty("aad.signInPolicy");
    static final String PW_RESET_POLICY = Config.getProperty("aad.passwordResetPolicy");
    static final String EDIT_PROFILE_POLICY = Config.getProperty("aad.editProfilePolicy");
    static final String CLIENT_ID = Config.getProperty("aad.clientId");
    static final String SECRET = Config.getProperty("aad.secret");
    static final String SCOPES = Config.getProperty("aad.scopes");
    static final String SIGN_OUT_ENDPOINT = Config.getProperty("aad.signOutEndpoint");
    static final String POST_SIGN_OUT_FRAGMENT = Config.getProperty("aad.postSignOutFragment");
    static final Long STATE_TTL = Long.parseLong(Config.getProperty("app.stateTTL"));
    static final String REDIRECT_URI = Config.getProperty("app.redirectUri");
    static final String HOME_PAGE = Config.getProperty("app.homePage");
    static final String FORGOT_PASSWORD_ERROR_CODE = Config.getProperty("aad.forgotPasswordErrCode");


    public static ConfidentialClientApplication getConfidentialClientInstance(String authorityWithPolicy)
            throws Exception {
        ConfidentialClientApplication confClientInstance = null;
        Config.logger.log(Level.INFO, "Getting confidential client instance");
        try {
            IClientSecret secret = ClientCredentialFactory.createFromSecret(SECRET);
            confClientInstance = ConfidentialClientApplication.builder(CLIENT_ID, secret)
                    .b2cAuthority(authorityWithPolicy).build();
        } catch (final Exception ex) {
            Config.logger.log(Level.SEVERE, "Failed to create Confidential Client Application.");
            throw ex;
        }
        return confClientInstance;
    }

    public static void redirectToSignoutEndpoint(final HttpServletRequest req, final HttpServletResponse resp)
            throws Exception {
        req.getSession().invalidate();
        String redirect = String.format("%s%s%s%s%s", AUTHORITY, SIGN_IN_POLICY, SIGN_OUT_ENDPOINT,
                POST_SIGN_OUT_FRAGMENT, URLEncoder.encode(HOME_PAGE, "UTF-8"));
        Config.logger.log(Level.INFO, "Redirecting user to {0}", redirect);
        resp.setStatus(302);
        resp.sendRedirect(redirect);
    }

    public static void signIn(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        Config.logger.log(Level.INFO, "sign-in sign-up flow init");
        AuthHelper.authorize(req, resp, SIGN_IN_POLICY);
    }

    public static void editProfile(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        Config.logger.log(Level.INFO, "edit profile flow init");
        AuthHelper.redirectToAuthorizationEndpoint(req, resp, EDIT_PROFILE_POLICY);
    }

    public static void passwordReset(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        Config.logger.log(Level.INFO, "password reset flow init");
        AuthHelper.redirectToAuthorizationEndpoint(req, resp, PW_RESET_POLICY);
    }

    private static void authorize(final HttpServletRequest req, final HttpServletResponse resp, String policy)
            throws Exception {

        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        Config.logger.log(Level.INFO, "doing authorize");
        IAuthenticationResult authResult = msalAuth.getAuthResult();
        if (authResult != null){
            Config.logger.log(Level.INFO, "try to silently acquire token");
            acquireTokenSilently(req, resp, policy, authResult.account());
        } else {
            Config.logger.log(Level.INFO, "try to interactive acquire token");
            redirectToAuthorizationEndpoint(req, resp, policy);
        }
        
    }

    private static void acquireTokenSilently(final HttpServletRequest req, final HttpServletResponse resp, final String policy, IAccount account) throws Exception {
        final MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());
        final SilentParameters parameters = SilentParameters.builder(Collections.singleton(SCOPES), account).build();

        try {
            ConfidentialClientApplication client = getConfidentialClientInstance(AUTHORITY + policy);
            client.tokenCache().deserialize(msalAuth.getTokenCache());
            Config.logger.log(Level.INFO, "preparing to acquire silently");
            CompletableFuture<IAuthenticationResult> future = client.acquireTokenSilently(parameters);
            IAuthenticationResult result = future.get();
            Config.logger.log(Level.INFO, "got future!");
            if (result != null) {
                Config.logger.log(Level.INFO, "silent auth success");
                parseJWTClaimsSetFromResultIntoSession(result, msalAuth);
                processSuccessfulAuthentication(msalAuth, client.tokenCache().serialize(), result);
                resp.setStatus(302);
                resp.sendRedirect(HOME_PAGE);
            } else {
                Config.logger.log(Level.INFO, "silent auth future is null! redirecting to authorize with code");
                redirectToAuthorizationEndpoint(req, resp, policy);
            }
            
        } catch (Exception ex) {
            Config.logger.log(Level.WARNING, "failed silent auth with exception! redirecting to authorize with code");
            Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            Config.logger.log(Level.WARNING, ex.getMessage());
            redirectToAuthorizationEndpoint(req, resp, policy);
        }
    }

    private static void redirectToAuthorizationEndpoint(final HttpServletRequest req, final HttpServletResponse resp, final String policy)
            throws Exception {
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();

        final MsalAuthSession msalAuthSession = getMsalAuthSession(req.getSession());
        msalAuthSession.setStateAndNonceAndPolicy(state, nonce, policy);

        ConfidentialClientApplication client = getConfidentialClientInstance(AUTHORITY + policy);

        final AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(REDIRECT_URI, Collections.singleton(SCOPES)).responseMode(ResponseMode.QUERY)
                .prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).build();

        final String redirectUrl = client.getAuthorizationRequestUrl(parameters).toString();
        Config.logger.log(Level.INFO, "Redirecting user to {0}", redirectUrl);
        resp.setStatus(302);
        resp.sendRedirect(redirectUrl);
    }

    public static void processAuthCodeRedirect(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        MsalAuthSession msalAuth = getMsalAuthSession(req.getSession());

        final String policy = msalAuth.getPolicy();
        Config.logger.log(Level.INFO, "session policy is {0}", policy);
        final String nonce = msalAuth.getNonce();
        Config.logger.log(Level.INFO, "session nonce is {0}", nonce);

        final String state = req.getParameter("state");
        Config.logger.log(Level.FINE, "state is {0}", state);
        final String authCode = req.getParameter("code");
        Config.logger.log(Level.FINE, "auth code is {0}", authCode);

        if (authCode != null && validateState(msalAuth, state)) {
            Config.logger.log(Level.INFO, "Received AuthCode and confirmed that state matches!");
            try {
                final AuthorizationCodeParameters authParams = AuthorizationCodeParameters
                        .builder(authCode, new URI(req.getRequestURL().toString()))
                        .scopes(Collections.singleton(SCOPES)).build();
                
                final ConfidentialClientApplication client = AuthHelper.getConfidentialClientInstance(AUTHORITY + policy);
                Future<IAuthenticationResult> future = client.acquireToken(authParams);
                IAuthenticationResult result = future.get();
                parseJWTClaimsSetFromResultIntoSession(result, msalAuth);
                
                if (validateNonce(msalAuth)){
                    processSuccessfulAuthentication(msalAuth, client.tokenCache().serialize(), result);
                } else {
                    throw new Exception("Couldn't exchange auth code for token");
                }
            } catch (Exception ex) {
                Config.logger.log(Level.WARNING, "Unable to exchange auth code for token");
                Config.logger.log(Level.WARNING, ex.getMessage());
                Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
                req.getSession().invalidate();
            }
        } else {
            Config.logger.log(Level.WARNING, "Auth code empty or state mismatch");
            req.getSession().invalidate();
        }
        resp.setStatus(302);
        resp.sendRedirect(HOME_PAGE);
    }

    private static void processSuccessfulAuthentication(MsalAuthSession msalAuth, String serializedTokenCache, IAuthenticationResult result){
        Config.logger.log(Level.INFO, "processing successful auth into sesssion");
        msalAuth.setTokenCache(serializedTokenCache);
        msalAuth.setAuthenticated(true);
        msalAuth.setUsername(msalAuth.getIdTokenClaims().get("name"));
        msalAuth.setAuthResult(result);
    }

    private static void parseJWTClaimsSetFromResultIntoSession(IAuthenticationResult result, MsalAuthSession msalAuth) {
        Config.logger.log(Level.INFO, "placing JWT claims set from auth result into session");
        try {
            SignedJWT idToken = SignedJWT.parse(result.idToken());
            JWTClaimsSet jcs = idToken.getJWTClaimsSet();
            msalAuth.setIdTokenClaims(jcs.getClaims());

        } catch (Exception ex) {
            Config.logger.log(Level.WARNING, "Failed to put claims into session: result was null or invalid");
        }
    }

    private static boolean validateState(MsalAuthSession msalAuth, final String stateFromRequest) {
        final String sessionState = msalAuth.getState();

        final Date now = new Date();
        if (sessionState == null
                || stateFromRequest == null 
                || !sessionState.equals(stateFromRequest)
                || msalAuth.getStateDate().before(new Date(now.getTime() - (STATE_TTL * 1000)))) {
            
            Config.logger.log(Level.WARNING, "State mismatch or null or empty or expired on validateState");
            return false;
        }
        msalAuth.setState(null); // don't allow re-use of state
        return true;
    }

    private static boolean validateNonce(MsalAuthSession msalAuth){
        String nonceClaim = msalAuth.getIdTokenClaims().get("nonce");
        String nonce = msalAuth.getNonce();
        if (nonce != null && nonce.equals(nonceClaim)) {
            msalAuth.setNonce(null); // don't allow re-use of nonce
            return true;
        }
        return false;
    }

    public static MsalAuthSession getMsalAuthSession(final HttpSession session) {
        return MsalAuthSession.getMsalAuthSession(session);
    }
}
