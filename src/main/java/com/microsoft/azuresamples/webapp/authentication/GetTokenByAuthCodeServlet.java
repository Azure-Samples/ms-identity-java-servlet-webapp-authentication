package com.microsoft.azuresamples.webapp.authentication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.azuresamples.webapp.AuthHelper;
import com.microsoft.azuresamples.webapp.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

@WebServlet(name = "GetTokenByAuthCodeServlet", urlPatterns = "/auth_redirect")
public class GetTokenByAuthCodeServlet extends HttpServlet {
    
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            AuthHelper.processAuthCodeRedirect(req);
        } catch (Exception e) {
            Config.logger.log(Level.WARNING, "Unable to process getting token by Auth Code: /auth_redirect endpoint");
            Config.logger.log(Level.WARNING, e.getMessage());
            Config.logger.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
            
        }
        resp.setStatus(302);
        resp.sendRedirect(req.getContextPath().toString() + "/auth_sign_in_status");
    }

}


