<li class="nav-item">
    <a class="nav-link" href="{{ url_for('auth.token_details') }}">Hello <% out.print(name); %>!</a>
</li>
<li>
    <a class="btn btn-warning" href="{{ url_for('auth.sign_out') }}">Sign Out</a>
</li>
