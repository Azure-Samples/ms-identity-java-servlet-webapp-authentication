<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card">
    <h5 class="card-header bg-primary">
        403: Forbidden
    </h5>
    <div class="card-body">
        <p class="card-text">
            Visiting this page requires the signed in user to be assigned to <strong>the correct group(s)</strong> as defined in the authentication.properties file.
            <br>
            Click here to check the <a class="btn btn-success" href="<c:url value="/groups"></c:url>">Groups</a> page to see if you are a member of too many groups. Membership in over 200 groups will preclude groups from being emitted in the ID token.
        </p>
    </div>
</div>
