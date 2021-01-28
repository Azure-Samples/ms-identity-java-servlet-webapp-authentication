// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.authentication.graph.GraphHelper;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.User;

/**
 * This class defines the endpoint for showing the graph /me endpoint
 * This is here simply to demonstrate output of the graph call.
 */
@WebServlet(name = "CallGraphServlet", urlPatterns = "/call_graph")
public class CallGraphServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = GraphHelper.getGraphClient(req, resp).me().buildRequest().get();

            req.setAttribute("user", GraphUserProperties(user));
            req.setAttribute("bodyContent", "auth/graph.jsp");
            final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
            view.forward(req, resp);

        } catch (AuthException ex) {
            Config.logger.log(Level.WARNING, ex.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format("../auth_error_details?details=%s", ex.getMessage())));
        } catch (ClientException ex) {
            Config.logger.log(Level.WARNING, ex.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format("../auth_error_details?details=%s", ex.getMessage())));
        }
    }

    /**
     * Take a few of the User properties obtained from the graph /me endpoint and put them into KV pairs for UI to display.
     * @param user User object (Graph SDK com.microsoft.graph.models.extensions.User)
     * @return HashMap<String,String> select Key-Values from User object
     */
    private HashMap<String,String> GraphUserProperties(User user) {
        HashMap<String,String> userProperties = new HashMap<>();
        userProperties.put("Display Name", user.displayName);
        userProperties.put("Phone Number", user.mobilePhone);
        userProperties.put("City", user.city);
        userProperties.put("Given Name", user.givenName);
        return userProperties;
    }
}
