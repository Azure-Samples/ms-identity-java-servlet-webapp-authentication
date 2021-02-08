// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.roles;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

import com.fasterxml.jackson.databind.ObjectMapper;

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

		// send 403 forbidden error when relevant app roles are missing

		
		if (request.getRequestURI().contains("regular_user")) {
			Set<String> roles = getRoles(msalAuth);
			if (roles.isEmpty() || !roles.contains(REGULAR_USER)) {
				redirectUnauthenticatedUser(request, response, UNAUTHORIZED_REGULAR_USER_MESSAGE);
				return;
			}
		}

		if (request.getRequestURI().contains("privileged_admin")) {
			Set<String> roles = getRoles(msalAuth);
			if (roles.isEmpty() || !roles.contains(PRIVILEGED_ADMIN)) {
				redirectUnauthenticatedUser(request, response, UNAUTHORIZED_PRIVILEGED_ADMIN_MESSAGE);
				return;
			}
		}
		chain.doFilter(req, res);
	}

	private void redirectUnauthenticatedUser(HttpServletRequest request, HttpServletResponse response, String message)
			throws ServletException, IOException {
		Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
		request.setAttribute("bodyContent", "auth/403.jsp");
		request.setAttribute("details", message);
		final RequestDispatcher view = request.getRequestDispatcher("index.jsp");
		view.forward(request, response);

	}

	private Set<String> getRoles(MsalAuthSession msalAuth) throws IOException {
		Map<String, String> idTokenClaims = msalAuth.getIdTokenClaims();
		if (idTokenClaims == null)
			return Collections.emptySet();
		ObjectMapper mapper = new ObjectMapper();
		String roles = idTokenClaims.get(ROLES);
		if (roles == null) {
			return Collections.emptySet();
		}
		String[] rolesList = mapper.readValue(roles, String[].class);
		return new HashSet<>(Arrays.asList(rolesList));
	}

}
