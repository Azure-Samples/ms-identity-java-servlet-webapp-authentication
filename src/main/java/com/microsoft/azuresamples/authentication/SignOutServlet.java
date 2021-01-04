package com.microsoft.azuresamples.authentication;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class defines the endpoint for processing sign out
 * MSAL Java apps using this sample repo's paradigm will require this.
 */
@WebServlet(name = "SignOutServlet", urlPatterns = "/auth_sign_out")
public class SignOutServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            AuthHelper.signOut(req, resp);
        } catch (Exception ex){
            Config.logger.log(Level.WARNING, "Unable to sign out");
            Config.logger.log(Level.WARNING, ex.getMessage());
            Config.logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
        }
    }
}