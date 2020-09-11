package com.microsoft.azuresamples.webapp.authentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MsalAuthSession implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String SESSION_KEY = "msalAuth";
    private boolean authenticated = false;
    private String username = null;
    private List<String> idTokenClaims = new ArrayList<>();
    private String tokenAcquisitionResult = null;

    public List<String> getIdTokenClaims() {
        return idTokenClaims;
    }

    public void setIdTokenClaims(final List<String> idTokenClaims) {
        this.idTokenClaims = idTokenClaims;
    }

    public String getTokenAcquisitionResult() {
        return tokenAcquisitionResult;
    }

    public void setTokenAcquisitionResult(final String tokenAcquisitionResult) {
        this.tokenAcquisitionResult = tokenAcquisitionResult;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(final boolean authenticated) {
        this.authenticated = authenticated;
    }
}
