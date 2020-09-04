<% List<String> exclude_claims = new ArrayList<>(); %>

<div class="card">
    <h5 class="card-header bg-primary">
        ID Token Details
    </h5>
    <div class="card-body">
        <!-- <h5 class="card-title"></h5> -->
        <p class="card-text">
            <%if (session.msal_authenticated == true) {
                for claim, value in session.msal_id_token_claims.items(){
                    if (claim not in exclude_claims)
                        out.println(String.format("<b> %s:</b> %s </br>", claim, value));
                }
            }
        </p>
        <!-- <div class="card-footer"></div> -->
    </div>
</div>

