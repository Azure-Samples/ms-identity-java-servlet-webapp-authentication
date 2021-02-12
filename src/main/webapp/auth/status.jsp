<jsp:useBean id="msalAuth" scope="session"
	class="com.microsoft.azuresamples.roles.MsalAuthSession" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="card">
	<h5 class="card-header bg-primary">
		<%
			out.println(msalAuth.getAuthenticated() ? "You're signed in!" : "You're not signed in.");
		%>
	</h5>
	<div class="card-body">
		<p class="card-text">
			<%
				if (msalAuth.getAuthenticated()) {
			%>
		
		<h2>Integrate a Java servlet web app that uses App roles with the Microsoft Identity Platform</h2>
		<p>This sample shows how to build a Java servlet Web app that uses
			Azure AD app roles for authorization.</p>
		<br />
		<div class="btn-group">
			<a class="btn btn-info"
				href="<c:url value="./privileged_admin"></c:url>">Admin Page</a> <a
				class="btn btn-info" href="<c:url value="./regular_user"></c:url>">User
				Page</a> <a class="btn btn-success"
				href="<c:url value="./token_details"></c:url>">ID Token Details</a>
		</div>
		<%
			} else {
		%>
		Use the button on the top right to sign in. Attempts to get your <a
			href="<c:url value="./token_details"></c:url>">ID Token Details</a>
		will result in a 401 error.
		<%
			}
		%>
		</p>
	</div>
</div>
