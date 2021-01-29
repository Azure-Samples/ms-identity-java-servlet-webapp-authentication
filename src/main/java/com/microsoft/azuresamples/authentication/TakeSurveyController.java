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
 * This class defines a page for showing the user their sign in status
 * This is also the home page.
 * This is here only for sample demonstration purposes.
 */
@WebServlet(name = "UserController", urlPatterns = {"/demo_survey"})
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