// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.roles;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class defines a page for showing the user their token details
 * This is here only for sample demonstration purposes.
 */
@WebServlet(name = "TokenDetailsServlet", urlPatterns = "/token_details")
public class TokenDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {        
        final HashMap<String,String> filteredClaims = filterClaims(req);

        req.setAttribute("claims", filteredClaims);
        req.setAttribute("bodyContent", "auth/token.jsp");
        final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
        view.forward(req, resp);
    }

    private HashMap<String,String> filterClaims(HttpServletRequest request) {
        MsalAuthSession msalAuth = MsalAuthSession.getMsalAuthSession(request.getSession());

        final String[] claimKeys = {"sub", "aud", "ver", "iss", "name", "roles","oid", "preferred_username", "nonce", "tid"};
        final List<String> includeClaims = Arrays.asList(claimKeys);

        HashMap<String,String> filteredClaims = new HashMap<>();
        msalAuth.getIdTokenClaims().forEach((k,v) -> {
            if (includeClaims.contains(k))
                filteredClaims.put(k, v);
        });
        return filteredClaims;
    }
}
