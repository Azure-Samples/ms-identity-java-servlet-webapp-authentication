
package com.microsoft.azuresamples.webapp.authentication;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@WebServlet(
        name = "TokenDetailsServlet",
        urlPatterns = "/auth_token_details"

)
public class TokenDetailsServlet extends HttpServlet {

    private final String[] exClaims = {"iat", "exp", "nbf", "uti", "aio"};
    private final List<String> excludeClaims = Arrays.asList(exClaims);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        AuthSessionAttributes sessionAttributes = (AuthSessionAttributes) req.getSession().getAttribute("msal_auth_session_attributes");
        req.setAttribute("name", "ADMIN");
        req.setAttribute("bodyContent", "auth/status.jsp");
        RequestDispatcher view = req.getRequestDispatcher("../index.jsp");
        view.forward(req, resp);
    }
}