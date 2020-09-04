<%@ page import ="java.util.*" %>
<!DOCTYPE html>
<html>
<body>
    <center>
    <h1>
        Microsoft Identity Platform - Authentication: Use MSAL Java to sign in B2C users
    </h1>
    <br />
    <%
        String GREETING_FORMAT = "Hello %s";
        String name= (String) request.getAttribute("name");
        out.println(String.format(GREETING_FORMAT, name));
    %>
</body>
</html>