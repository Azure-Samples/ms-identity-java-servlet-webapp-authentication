<jsp:useBean id="msalAuth" scope="session" class="com.microsoft.azuresamples.authentication.MsalAuthSession" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card">
    <h5 class="card-header bg-primary">
        <% out.println(msalAuth.getAuthenticated()? "You're signed in!" : "You're not signed in."); %>
    </h5>
    <div class="card-body">
        <p class="card-text">
            <% if (msalAuth.getAuthenticated()) { %>
            
            
			<h2>Integrating Azure AD V2 into an Java servlet Web app and
			using Azure AD app roles for authorization.</h2>
<p>
    This sample shows how to build a Java servlet Web app that uses Azure AD app roles for authorization. 
</p>
<br />

<h3 style="color:blue">Try one of the following Azure App Role driven operations</h3>

<table>
    <tr>
        <td></td>
        <td><a href="./demo_survey">Fill in a survey (you need to be a member of the 'SurveyTaker' role)</a></td>
    </tr>
    <tr>
        <td></td>
        <td><a href="./create_survey">Create a survey (you need to be a member of the 'SurveyCreator' role)</a></td>
    </tr>
</table>
<br />
            
                Click here to get your <a class="btn btn-success" href="<c:url value="./token_details"></c:url>">ID Token Details</a>
            <% } else { %>
                Use the button on the top right to sign in.
                Attempts to get your <a href="<c:url value="./token_details"></c:url>">ID Token Details</a>
                 will result in a 401 error.
            <% } %>
        </p>
    </div>
</div>
