package com.microsoft.azuresamples.authenticationb2c;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

@WebServlet(name = "AADRedirectServlet", urlPatterns = "/auth_redirect")
public class AADRedirectServlet extends HttpServlet {
    
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Config.logger.log(Level.FINE, "Request has come with params {0}", req.getQueryString());
        final String errorDescription = req.getParameter("error_description");

        try {
            //if there is an error & its description has password reset err code, do reset pw flow
            if (errorDescription != null && errorDescription.contains(AuthHelper.FORGOT_PASSWORD_ERROR_CODE)) {
                AuthHelper.passwordReset(req, resp);
            } else {
                AuthHelper.processAuthCodeRedirect(req, resp);
            }
        } catch (Exception e) {
            Config.logger.log(Level.WARNING, "Unable to process getting token by Auth Code: /auth_redirect endpoint");
            Config.logger.log(Level.WARNING, e.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
            
        }
    }

}
