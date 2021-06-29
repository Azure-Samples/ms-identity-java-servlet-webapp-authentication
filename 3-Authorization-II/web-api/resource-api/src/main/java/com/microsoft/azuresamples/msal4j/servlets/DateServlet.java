// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.msal4j.helpers.Config;
import com.nimbusds.jwt.JWTClaimsSet;


/**
 * This class defines the endpoint for showing the graph /me endpoint
 * This is here simply to demonstrate the graph call.
 */
@WebServlet(name = "DateServlet", urlPatterns = "/api/date")
public class DateServlet extends HttpServlet {
    private static final String REQUIRED_SCOPES = Config.SCOPES; 
    private static final Logger logger = Logger.getLogger(DateServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // the AzureAccessTokenFilter should have placed the AT in the req if validation was successful.
        // however, the scopes claim remains for each endpoint to check in our sample
        // (assuming each endpoint might have unique scope requirements)
        JWTClaimsSet atClaims = (JWTClaimsSet) req.getAttribute("accessToken");
        try {
            String scope = atClaims.getStringClaim("scp");
            if (!REQUIRED_SCOPES.equals(scope)){
                // boot the user out! They don't have required scopes.
                throw new ServletException("Access token does not have required scopes");
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
            throw new ServletException(e.getMessage());
        }
        PrintWriter out;
        out = resp.getWriter();
        resp.setContentType("text/html");
        resp.setStatus(200);
        out.write(dateAsJson());
        out.close();
    }

    private String dateAsJson() {
        final Date now = new Date();
        final String humanReadable = now.toString();
        final String timeStamp = Long.toString(now.getTime());
        return String.format("%s,%s", humanReadable, timeStamp);
    }
}
