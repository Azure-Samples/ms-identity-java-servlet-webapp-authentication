package com.microsoft.azuresamples.webapp.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.webapp.AuthHelper;

@WebServlet(name = "SignInServlet", urlPatterns = "/auth_sign_in")
public class SignInServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            AuthHelper.redirectToAuthorizationEndpoint(req, resp);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}