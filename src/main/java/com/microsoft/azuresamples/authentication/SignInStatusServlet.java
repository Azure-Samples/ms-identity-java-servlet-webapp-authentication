
package com.microsoft.azuresamples.authentication;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class defines a page for showing the user their sign in status
 * This is here only for sample demonstration purposes.
 */
@WebServlet(name = "SignInStatusServlet", urlPatterns = "/auth_sign_in_status")
public class SignInStatusServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("bodyContent", "auth/status.jsp");
        final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
        view.forward(req, resp);
    }
}