package com.microsoft.azuresamples.authentication.graph;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azuresamples.authentication.AuthException;
import com.microsoft.azuresamples.authentication.AuthHelper;
import com.microsoft.azuresamples.authentication.MsalAuthSession;

import okhttp3.Request;

public class GraphHelper {
    /**
     * getGraphClient prepares and returns a graphServiceClient to make API calls to Graph. See docs for GraphServiceClient (GraphSDK for Java)
     * @param servletRequest
     * @param servletResponse
     * @return GraphServiceClient
     * @throws IOException
     * @throws AuthException
     */
    public static IGraphServiceClient getGraphClient(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, AuthException{
        return GraphServiceClient
        .builder()
        .authenticationProvider(new MsalGraphAuthenticationProvider(servletRequest, servletResponse))
        .buildClient();
    }
}

/**
 * Our Msal Graph Authentication Provider class. Required for setting up a GraphServiceClient
 */
class MsalGraphAuthenticationProvider implements ICoreAuthenticationProvider, IAuthenticationProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    private String authorizationHeaderValue;

    /** Set up the MsalGraphAuthenticationProvider
     * 1. Re-Authorize (silent preference)
     * 2. Get Token and put it into authorizationHeaderValue (used by the authenticateRequest methods)
    */

    /**
     * @param servletRequest
     * @param servletResponse
     * @throws IOException
     * @throws AuthException
     */
    public MsalGraphAuthenticationProvider(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, AuthException{
        AuthHelper.authorize(servletRequest, servletResponse);
		authorizationHeaderValue = String.format("%s %s", BEARER, MsalAuthSession.getMsalAuthSession(servletRequest.getSession()).getAuthResult().accessToken());
    }

    /** 
     * This implementation of the ICoreAuthenticationProvider injects the Graph access token
     * from Azure AD into the headers of the okhttp3.Request used by GraphSDK/GraphSDKCore.
     * This is the new format that the GraphSDK will use going forward
    */
    @Override
	public Request authenticateRequest(Request request) {
		return request.newBuilder().addHeader(AUTHORIZATION_HEADER, authorizationHeaderValue).build();
	}
    
    // This implementation of the IAuthenticationProvider injects the Graph access token
    // form Azure AD into the headers of the IHttp request used by GraphSDK.
    // This is used in the GraphSDK but with a deprecation warning.
	@Override
	public void authenticateRequest(IHttpRequest request) {
		request.addHeader(AUTHORIZATION_HEADER, authorizationHeaderValue);
	}

}
