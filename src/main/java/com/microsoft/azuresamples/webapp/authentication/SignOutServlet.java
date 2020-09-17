package com.microsoft.azuresamples.webapp.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.webapp.AuthHelper;

@WebServlet(name = "SignOutServlet", urlPatterns = "/auth_sign_out")
public class SignOutServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        AuthHelper.doAuthorizationRequest(req, resp);
    }
}