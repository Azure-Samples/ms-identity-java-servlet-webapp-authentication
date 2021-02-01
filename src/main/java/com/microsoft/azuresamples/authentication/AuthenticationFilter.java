// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

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
	static final String SURVEY_CREATOR = "SurveyCreator";
	static final String SURVEY_TAKER = "SurveyTaker";
	static final String UNAUTHORIZED_SURVEY_CREATOR_MESSAGE = "UNAUTHORIZED user! role " + SURVEY_CREATOR
			+ " is missing";
	static final String UNAUTHORIZED_SURVEY_TAKER_MESSAGE = "UNAUTHORIZED user! role " + SURVEY_TAKER + " is missing";

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

		if (request.getRequestURI().contains("take_survey")) {
			Map<String, String> idTokenClaims = msalAuth.getIdTokenClaims();
			String roles = idTokenClaims.get(ROLES);
			if (roles == null || !roles.contains(SURVEY_TAKER)) {
				Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
				response.sendRedirect(response.encodeRedirectURL(
						String.format("auth_error_details?details=%s", UNAUTHORIZED_SURVEY_TAKER_MESSAGE)));
				return;
			}

		}

		if (request.getRequestURI().contains("create_survey")) {

			Map<String, String> idTokenClaims = msalAuth.getIdTokenClaims();
			String roles = idTokenClaims.get(ROLES);
			if (roles == null || !roles.contains(SURVEY_CREATOR)) {
				Config.logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
				response.sendRedirect(response.encodeRedirectURL(
						String.format("auth_error_details?details=%s", UNAUTHORIZED_SURVEY_CREATOR_MESSAGE)));
				return;
			}
		}

		chain.doFilter(req, res);

	}
}
