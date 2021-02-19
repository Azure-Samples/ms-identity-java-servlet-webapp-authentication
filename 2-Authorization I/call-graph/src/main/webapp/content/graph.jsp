<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card">
    <h5 class="card-header bg-primary">
        Call Graph /me Endpoint
    </h5>
    <div class="card-body">
        <p class="card-text">
            <c:forEach items="${user}" var="user">
                <strong>${user.key}:</strong> ${user.value}
                <br>
            </c:forEach>
            <br>
            Click here to see your <a class="btn btn-success" href="<c:url value="./sign_in_status"></c:url>">Sign-in Status</a>
            or <a class="btn btn-success" href="<c:url value="./token_details"></c:url>">Token Details</a>
        </p>
    </div>
</div>
