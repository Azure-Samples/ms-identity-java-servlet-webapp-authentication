package com.microsoft.azuresamples.webapp.authentication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetTokenByAuthCodeServlet", urlPatterns = "/auth_redirect")
public class GetTokenByAuthCodeServlet extends HttpServlet {

}


