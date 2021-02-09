<jsp:useBean id="msalAuth" scope="session" class="com.microsoft.azuresamples.msal4j.helpers.IdentityContextData"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card">
    <h5 class="card-header bg-primary">
        ID Token Details
    </h5>
    <div class="card-body">
        <p class="card-text">
            <c:forEach items="${claims}" var="claim">
                <strong>${claim.key}:</strong> ${claim.value}
                <br>
                <a class="btn btn-warning" href="<c:url value="/overage"></c:url>">Handle Groups Overage</a>
                <br>
            </c:forEach>
            <br>
            Click here to see your <a class="btn btn-success" href="<c:url value="/sign_in_status"></c:url>">Sign-in Status</a>
            or <a class="btn btn-success" href="<c:url value="/call_graph"></c:url>">Call Graph</a>
        </p>
    </div>
</div>
