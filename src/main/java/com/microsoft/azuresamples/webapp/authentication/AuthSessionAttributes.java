package com.microsoft.azuresamples.webapp.authentication;

import java.util.ArrayList;
import java.util.List;

// TODO: to string and from string?
public class AuthSessionAttributes{
    boolean authenticated = false;
    String username = null;
    List<String> IdTokenClaims = new ArrayList<>();
    String tokenAcquisitionResult = null;

    public List<String> getIdTokenClaims() {
        return IdTokenClaims;
    }

    public void setIdTokenClaims(List<String> idTokenClaims) {
        IdTokenClaims = idTokenClaims;
    }

    public String getTokenAcquisitionResult() {
        return tokenAcquisitionResult;
    }

    public void setTokenAcquisitionResult(String tokenAcquisitionResult) {
        this.tokenAcquisitionResult = tokenAcquisitionResult;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    boolean getAuthenticated() {
        return authenticated;
    }
    void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
