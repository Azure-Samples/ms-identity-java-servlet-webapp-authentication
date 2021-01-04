package com.microsoft.azuresamples.authentication;

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
@WebServlet(name = "HomePageServlet", urlPatterns = "/index")
public class HomePageServlet extends HttpServlet {
    
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/auth_sign_in_status").forward(req, resp);
    }

}
