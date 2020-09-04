<%String name=(String)request.getAttribute("name");
if(name != null){ %>
    <%@ include file="auth-bar-authenticated.jsp" %>
<% }
else {%>
    <%@ include file="auth-bar-not-authenticated.jsp" %>
<% } %>