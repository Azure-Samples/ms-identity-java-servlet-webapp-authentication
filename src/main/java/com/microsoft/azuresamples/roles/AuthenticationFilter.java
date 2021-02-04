// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.roles;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class implements filters All incoming requests go through this. This is
 * to redirect unauthorized clients away from protected routes
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {
	static final String ROLES = "roles";
	static final String PRIVILEGED_ADMIN = "PrivilegedAdmin";
	static final String REGULAR_USER = "RegularUser";
	static final String UNAUTHORIZED_PRIVILEGED_ADMIN_MESSAGE = "UNAUTHORIZED user! role " + PRIVILEGED_ADMIN
			+ " is missing";
	static final String UNAUTHORIZED_REGULAR_USER_MESSAGE = "UNAUTHORIZED user! role " + REGULAR_USER + " is missing";

	String[] protectedEndpoints = { "token_details" };

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		MsalAuthSession msalAuth = MsalAuthSession.getMsalAuthSession(request.getSession());

		// send 401 for unauthorized access to the protected endpoints
		
		if (Arrays.stream(protectedEndpoints).anyMatch(request.getRequestURI()::contains)
				&& !msalAuth.getAuthenticated()) {
			req.setAttribute("bodyContent", "auth/401.jsp");
			final RequestDispatcher view = request.getRequestDispatcher("index.jsp");
			view.forward(request, response);
			return;
		}

		if (request.getRequestURI().contains("regular_user")) {
			Map<String, String> idTokenClaims = msalAuth.getIdTokenClaims();
			String roles = idTokenClaims.get(ROLES);
			if (roles == null || !roles.contains(REGULAR_USER)) {
				Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
				response.sendRedirect(response.encodeRedirectURL(
						String.format("auth_error_details?details=%s", UNAUTHORIZED_REGULAR_USER_MESSAGE)));
				return;
			}

		}

		if (request.getRequestURI().contains("privileged_admin")) {

			Map<String, String> idTokenClaims = msalAuth.getIdTokenClaims();
			String roles = idTokenClaims.get(ROLES);
			if (roles == null || !roles.contains(PRIVILEGED_ADMIN)) {
				Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
				response.sendRedirect(response.encodeRedirectURL(
						String.format("auth_error_details?details=%s", UNAUTHORIZED_PRIVILEGED_ADMIN_MESSAGE)));
				return;
			}
		}

		chain.doFilter(req, res);

	}
}
