package com.microsoft.azuresamples.webapp.authentication;

import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MsalAuthSession implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String SESSION_KEY = "msalAuth";
    private String nonce = null;
    private String state = null;
    private Date stateDate = null;
    private boolean authenticated = false;
    private String username = null;
    private Map<String,String> idTokenClaims = new HashMap<>();
    private String tokenCache = "";
    private transient HttpSession session;

    public static MsalAuthSession getMsalAuthSession(final HttpSession session) {
        MsalAuthSession msalAuth =(MsalAuthSession) session.getAttribute(MsalAuthSession.SESSION_KEY);
        if ( msalAuth == null) {
            msalAuth = new MsalAuthSession();
            session.setAttribute(MsalAuthSession.SESSION_KEY, msalAuth);
        }
        msalAuth.session = session;
        return msalAuth;
    }

    public void saveMsalAuthSession() {
        this.session.setAttribute(MsalAuthSession.SESSION_KEY, this);
    }

    public Map<String,String> getIdTokenClaims() {
        return idTokenClaims;
    }

    public String getTokenCache() {
        return this.tokenCache;
    }

    public String getUsername() {
        return username;
    }

    public boolean getAuthenticated() {
        return authenticated;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getState() {
        return this.state;
    }

    public Date getStateDate(){
        return this.stateDate;
    }

    public void setIdTokenClaims(final Map<String,Object> idTokenClaims) {
        this.idTokenClaims = new HashMap<>();
        idTokenClaims.forEach((String claim, Object value) -> {
            String val = value.toString();
            this.idTokenClaims.put(claim, val);
        });
        this.saveMsalAuthSession();
    }

    public void setTokenCache(final String serializedTokenCache) {
        this.tokenCache = serializedTokenCache;
        this.saveMsalAuthSession();
    }

    public void setUsername(final String username) {
        this.username = username;
        this.saveMsalAuthSession();
    }

    public void setAuthenticated(final boolean authenticated) {
        this.authenticated = authenticated;
        this.saveMsalAuthSession();
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
        this.saveMsalAuthSession();
    }

    public void setState(final String state){
        this.state = state;
        this.stateDate = new Date();
        this.saveMsalAuthSession();
    }

    public void setStateAndNonce(String state, String nonce) {
        this.state = state;
        this.nonce = nonce;
        this.stateDate = new Date();
        this.saveMsalAuthSession();
    }
}
