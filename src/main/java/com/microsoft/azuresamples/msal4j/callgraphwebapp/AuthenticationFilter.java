// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.callgraphwebapp;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.msal4j.helpers.IdentityContextData;
import com.microsoft.azuresamples.msal4j.helpers.ServletContextAdapter;

/**
 * This class implements filters
 * All incoming requests go through this.
 * This is to redirect unauthorized clients away from protected routes
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    String [] protectedEndpoints = {"token_details", "call_graph"};

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        IdentityContextData context = new ServletContextAdapter(request, response).getContext();

        // send 401 for unauthorized access to the protected endpoints
        if (Arrays.stream(protectedEndpoints).anyMatch(request.getRequestURI()::contains) && !context.getAuthenticated()) {
            req.setAttribute("bodyContent", "content/401.jsp");
            final RequestDispatcher view = request.getRequestDispatcher("index.jsp");
            view.forward(request, response);
        } else {
            chain.doFilter(req, res);
        }
    }
}
