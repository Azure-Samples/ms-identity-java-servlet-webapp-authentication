// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class defines the endpoint for showing the graph /me endpoint
 * This is here simply to demonstrate output of the graph call.
 */
@WebServlet(name = "CallGraphServlet", urlPatterns = "/call_graph")
public class CallGraphServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            AuthHelper.authorize(req, resp);
        } catch (AuthException ex) {
            Config.logger.log(Level.WARNING, ex.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format("../auth_error_details?details=%s", ex.getMessage())));
        }
    }
}
