package com.microsoft.azuresamples.webapp;

import com.microsoft.aad.msal4j.*;

import java.net.MalformedURLException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;



public class AuthHelper {
    private ConfidentialClientApplication confClientInstance;
    private String AUTHORITY = Config.getProperty("aad.authority");
    private String CLIENT_ID = Config.getProperty("aad.clientId");
    private String SECRET = Config.getProperty("aad.secret");
    private String REDIRECT_URI = Config.getProperty("app.redirectUri");

    public ConfidentialClientApplication getConfidentialClientInstance() {
        if (confClientInstance == null)
            return instantiateConfidentialClient();
        return confClientInstance;
    }

    private String getAuthorizationRequestUrl(String state, String nonce) {
        AuthorizationRequestUrlParameters parameters =
                AuthorizationRequestUrlParameters
                        .builder(REDIRECT_URI,
                                Collections.singleton("openid offline_access profile"))
                        .responseMode(ResponseMode.QUERY)
                        .prompt(Prompt.SELECT_ACCOUNT)
                        .state(state)
                        .nonce(nonce)
                        .build();

        return getConfidentialClientInstance().getAuthorizationRequestUrl(parameters).toString();
    }

    private ConfidentialClientApplication instantiateConfidentialClient() {
        try {
            confClientInstance = ConfidentialClientApplication.builder(
                    CLIENT_ID,
                    ClientCredentialFactory.createFromSecret(SECRET))
                    .b2cAuthority(AUTHORITY)
                    .build();
        } catch (MalformedURLException ex) {
            System.out.println("Failed to create Confidential Client Application");
        }
        return confClientInstance;
    }


}
