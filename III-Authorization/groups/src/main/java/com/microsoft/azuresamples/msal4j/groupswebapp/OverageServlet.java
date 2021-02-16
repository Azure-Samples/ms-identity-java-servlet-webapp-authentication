// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.groupswebapp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.msal4j.helpers.GraphHelper;
import com.microsoft.azuresamples.msal4j.helpers.IdentityContextAdapterServlet;
import com.microsoft.azuresamples.msal4j.helpers.IdentityContextData;
import com.microsoft.graph.models.extensions.Group;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines the endpoint for processing the redirect from AAD MSAL
 * Java apps using this sample's paradigm will require this.
 */
@WebServlet(name = "OverageServlet", urlPatterns = "/overage")
public class OverageServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(OverageServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.log(Level.FINE, "Request has come with params {0}", req.getQueryString());
        try {
            IdentityContextAdapterServlet contextAdapter = new IdentityContextAdapterServlet(req, resp);
            IdentityContextData context = contextAdapter.getContext();
            List<Group> groups = GraphHelper.getGroups(GraphHelper.getGraphClient(context.getAccessToken()));
            context.setGroups(groups);
            contextAdapter.saveContext();
            resp.sendRedirect("groups");
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage());
            logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            try {
                RequestDispatcher rd = req.getRequestDispatcher(String.format("/auth_error_details?details=%s", URLEncoder.encode(ex.getMessage(), "UTF-8")));
                rd.forward(req, resp);
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
    }

}
