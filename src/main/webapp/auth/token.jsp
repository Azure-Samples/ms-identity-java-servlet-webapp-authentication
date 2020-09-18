<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card">
    <h5 class="card-header bg-primary">
        ID Token Details
    </h5>
    <div class="card-body">
        <!-- <h5 class="card-title"></h5> -->
        <p class="card-text">
            <c:forEach items="${claims}" var="claim">
                <b> ${claim.key} :</b> ${claim.value} <br/>
            </c:forEach>
        </p>
        <!-- <div class="card-footer"></div> -->
    </div>
</div>
