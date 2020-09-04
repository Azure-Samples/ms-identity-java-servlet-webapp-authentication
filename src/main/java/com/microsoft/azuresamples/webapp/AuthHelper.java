package com.microsoft.azuresamples.webapp;

import com.microsoft.aad.msal4j.*;

import java.net.MalformedURLException;

public class AuthHelper {
    private ConfidentialClientApplication confClientInstance;

    private ConfidentialClientApplication getConfidentialClientInstance() {
        if (confClientInstance == null)
            return instantiateConfidentialClient();
        return confClientInstance;
    }

    private ConfidentialClientApplication instantiateConfidentialClient() {
        try {
            confClientInstance = ConfidentialClientApplication.builder(
                    Config.getProperty("aad.clientId"),
                    ClientCredentialFactory.createFromSecret(Config.getProperty("aad.secret")))
                    .b2cAuthority(Config.getProperty("aad.authority"))
                    .build();
        } catch (MalformedURLException ex) {
            System.out.println("Failed to create Confidential Client Application");
        }
        return confClientInstance;
    }


}
