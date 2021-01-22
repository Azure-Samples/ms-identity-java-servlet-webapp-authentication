// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * This class defines the endpoint for processing the redirect from AAD MSAL
 * Java apps using this sample repository's paradigm will require this.
 */
@WebServlet(name = "AADRedirectServlet", urlPatterns = "/auth/redirect")
public class AADRedirectServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        Config.logger.log(Level.FINE, "Request has come with params {0}", req.getQueryString());
        try {
            AuthHelper.processAADCallback(req, resp);
            Config.logger.log(Level.INFO, "redirecting to home page.");
            resp.sendRedirect(Config.getProperty("app.homePage"));
        } catch (AuthException ex) {
            Config.logger.log(Level.WARNING, ex.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format("../auth_error_details?details=%s", ex.getMessage())));
        }
    }

}
