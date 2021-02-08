<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="card">
	<h5 class="card-header bg-primary">ID Token Details</h5>
	<div class="card-body">
		<p class="card-text">
			<c:forEach items="${claims}" var="claim">
				<strong>${claim.key}:</strong> ${claim.value}
                <br>
			</c:forEach>
			<br>
		<div class="btn-group">
			<a class="btn btn-success"
				href="<c:url value="./sign_in_status"></c:url>">Sign-in Status</a> <a
				class="btn btn-info"
				href="<c:url value="./privileged_admin"></c:url>">Admin Page</a> <a
				class="btn btn-info" href="<c:url value="./regular_user"></c:url>">User
				Page</a>
		</div>
	</div>
</div>
