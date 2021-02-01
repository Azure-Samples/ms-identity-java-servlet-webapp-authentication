// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.authentication;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 */
@WebServlet(name = "UserController", urlPatterns = {"/take_survey"})
public class TakeSurveyController extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	   @Override
	    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {


	            req.setAttribute("bodyContent", "auth/demoSurvey.jsp");
	            final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
	            view.forward(req, resp);
	                
	   }
}