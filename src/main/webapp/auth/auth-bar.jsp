<c:if test = "${msalAuth.getAuthenticated()}">
    <li class="nav-item">
        <a class="nav-link" href="{{ url_for('auth.token_details') }}">Hello <% out.print(msalAuth.getUsername()); %>!</a>
    </li>
    <li>
        <a class="btn btn-warning" href="{{ url_for('auth.sign_out') }}">Sign Out</a>
    </li>
</c:if>
<c:if test = "${!msalAuth.getAuthenticated()}">
    <li><a class="btn btn-success" href="{{ url_for('auth.sign_in') }}">Sign In</a></li>
</c:if>