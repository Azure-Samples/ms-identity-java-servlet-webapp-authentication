package com.microsoft.azuresamples.msal4j.helpers;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import okhttp3.Request;

public class GraphHelper {
    /**
     * getGraphClient prepares and returns a graphServiceClient to make API calls to Graph. See docs for GraphServiceClient (GraphSDK for Java)
     * @param servletRequest
     * @param servletResponse
     * @return GraphServiceClient
     */
    public static IGraphServiceClient getGraphClient(String accessToken) {
        return GraphServiceClient
        .builder()
        .authenticationProvider(new MsalGraphAuthenticationProvider(accessToken))
        .buildClient();
    }
}

/**
 * Our Msal Graph Authentication Provider class. Required for setting up a GraphServiceClient
 */
class MsalGraphAuthenticationProvider implements ICoreAuthenticationProvider, IAuthenticationProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    private String authTokenHeaderValue;

    /** Set up the MsalGraphAuthenticationProvider
     * Put `Bearer <token>` into authTokenHeaderValue (used by the authenticateRequest methods)
    */

    /**
     * @param accessToken
     */
    public MsalGraphAuthenticationProvider(String accessToken) {
		authTokenHeaderValue = String.format("%s %s", BEARER, accessToken);
    }

    /** 
     * This implementation of the ICoreAuthenticationProvider injects the Graph access token
     * from Azure AD into the headers of the okhttp3.Request used by GraphSDK/GraphSDKCore.
     * This is the new format that the GraphSDK will use going forward
    */
    @Override
	public Request authenticateRequest(Request request) {
		return request.newBuilder().addHeader(AUTHORIZATION_HEADER, authTokenHeaderValue).build();
	}
    
    // This implementation of the IAuthenticationProvider injects the Graph access token
    // form Azure AD into the headers of the IHttp request used by GraphSDK.
    // This is used in the GraphSDK but with a deprecation warning.
	@Override
	public void authenticateRequest(IHttpRequest request) {
		request.addHeader(AUTHORIZATION_HEADER, authTokenHeaderValue);
	}

}
