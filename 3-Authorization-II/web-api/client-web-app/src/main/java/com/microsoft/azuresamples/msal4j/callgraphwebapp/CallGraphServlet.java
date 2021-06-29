// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.callgraphwebapp;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.msal4j.helpers.AuthHelper;
import com.microsoft.azuresamples.msal4j.helpers.Config;
import com.microsoft.azuresamples.msal4j.helpers.IdentityContextAdapterServlet;

/**
 * This class defines the endpoint for showing the graph /me endpoint
 * This is here simply to demonstrate the graph call.
 */
@WebServlet(name = "CallGraphServlet", urlPatterns = "/call_api")
public class CallGraphServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(CallGraphServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            // re-auth (prefer silently) in case the access token is not valid anymore.
            IdentityContextAdapterServlet contextAdapter = new IdentityContextAdapterServlet(req, resp);
            AuthHelper.acquireTokenSilently(contextAdapter);
            
            String time = NetClient.get(contextAdapter.getContext().getAccessToken());

            req.setAttribute("bodyContent", "content/api.jsp");
            req.setAttribute("humanReadable", time.split(",")[0]);
            req.setAttribute("timeStamp", time.split(",")[1]);
            final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
            view.forward(req, resp);

        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage());
            logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format(req.getContextPath() + "/auth_error_details?details=%s", ex.getMessage())));
        }
    }

    private static class NetClient {
        public static String get(String accessToken) throws Exception {
            try {
    
                URL url = new URL(String.format("%s%s", Config.API_BASE_ADDRESS, Config.API_DATE_ENDPOINT));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
                conn.setRequestProperty("Accept", "html/text");
                boolean error = false;
                int status = conn.getResponseCode();
                if (status != 200) {
                    error = true;
                }
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String output;
                StringBuilder sb = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                conn.disconnect();
                String resp = sb.toString();
                if (error == true) {
                    throw new RuntimeException(String.format("HTTP Error code : %s. Message: %s", conn.getResponseCode(), resp));
                }
                return resp;
            } catch (Exception e) {
                logger.log(Level.WARNING,"Exception in API Get call: {0} ", e.getMessage());
                throw e;
            }
        }
    }
}
