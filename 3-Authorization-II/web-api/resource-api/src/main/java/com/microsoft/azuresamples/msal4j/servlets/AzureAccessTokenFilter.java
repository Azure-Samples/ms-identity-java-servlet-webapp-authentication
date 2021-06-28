// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.servlets;
import com.microsoft.aad.msal4j.HttpResponse;
import com.microsoft.azuresamples.msal4j.helpers.Config;
import com.microsoft.azuresamples.msal4j.helpers.JwtVerifier;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.nimbusds.jwt.JWTClaimsSet;


/**
 * This class implements filter All incoming requests go through this. This
 * sample uses this filter to redirect unauthorized clients away from protected
 * routes
 */
@WebFilter(filterName = "AzureAccessTokenFilter", urlPatterns = "/*")
public class AzureAccessTokenFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AzureAccessTokenFilter.class.getName());
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AZURE_PUBLIC_KEY_URL = String.format("%s/discovery/v2.0/keys", Config.AUTHORITY);

    JwtVerifier verifier;

    public AzureAccessTokenFilter() {
        try {
            // make an instance of the verifier (found in helpers.JwtVerifier)
            // use AUTHORITY value from config for issuer.
            // use CLIENT_ID value from config for audience.
            // use SCOPES value from config for scopes.
            String issuer;
            String audience;
            if (Config.VERSION.equals("1")) {
                issuer = String.format("%s/", Config.AUTHORITY);
                audience = String.format("api://%s", Config.CLIENT_ID);
            } else {
                issuer = String.format("%s/v2.0", Config.AUTHORITY);
                audience = Config.CLIENT_ID;
            }

            verifier = new JwtVerifier(AZURE_PUBLIC_KEY_URL, issuer, audience, Config.SCOPES);
        } catch(MalformedURLException e) {
            logger.log(Level.SEVERE, "FATAL: Could not initialize JwtVerifier due to malformed URL - check your azurePublicKeyUrl param!");
            System.exit(1);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest)req;
        if(httpRequest.getHeader(AUTHORIZATION_HEADER_NAME) == null ||
                !httpRequest.getHeader(AUTHORIZATION_HEADER_NAME).startsWith(TOKEN_PREFIX) ) {
            throw new ServletException("Missing or Invalid Authorization Header in Request");
        }

        String accessToken = httpRequest.getHeader(AUTHORIZATION_HEADER_NAME).replace(TOKEN_PREFIX, "");
        
        try {
            // process + verify the token and extract claims
            // if the verification is successful, you'll receive a JWTClaimsSet
            JWTClaimsSet claimsSet = verifier.getJWTClaimsSet(accessToken);
            // put it into the request so the request handling servlet can make use of its claims as neeed.
            req.setAttribute("accessToken", claimsSet);
        } catch (Exception e) {
            String msg = String.format("An error occurred when validating the JWT signature: %s", e.getMessage());
            logger.log(Level.WARNING, msg);
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            PrintWriter out;
            out = httpResponse.getWriter();
            httpResponse.setStatus(403);
            out.write(msg);
            out.close();
        }

        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

}