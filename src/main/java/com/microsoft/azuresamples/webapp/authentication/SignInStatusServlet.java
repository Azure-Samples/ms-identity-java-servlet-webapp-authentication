
package com.microsoft.azuresamples.webapp.authentication;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.microsoft.azuresamples.webapp.Config;
import java.io.IOException;


@WebServlet(name = "SignInStatusServlet", urlPatterns = "/auth_sign_in_status")
public class SignInStatusServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final MsalAuthSession msalAuth = Config.configureMsalSessionAttributes(req);
        req.setAttribute("bodyContent", "auth/status.jsp");
        final RequestDispatcher view = req.getRequestDispatcher("index.jsp");
        view.forward(req, resp);
    }
}