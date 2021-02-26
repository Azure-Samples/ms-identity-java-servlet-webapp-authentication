// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.models.extensions.Group;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IGroupCollectionPage;
import com.microsoft.graph.requests.extensions.IGroupCollectionRequest;
import com.microsoft.graph.requests.extensions.IGroupCollectionRequestBuilder;

import okhttp3.Request;

/**
 * GraphHelper class handles creating a Graph SDK client (IGraphServiceClient)
 * and has functions for common Microsoft Graph calls
 */
public class GraphHelper {
    private static Logger logger = Logger.getLogger(GraphHelper.class.getName());

    private GraphHelper() {
        throw new IllegalStateException("Utility class - don't instantiate");
    }

    /**
     * getGraphClient prepares and returns a graphServiceClient to make API calls to
     * Graph. See docs for GraphServiceClient (GraphSDK for Java)
     * 
     * @param accessToken String
     * @return GraphServiceClient IGraphServiceClient
     */
    public static IGraphServiceClient getGraphClient(String accessToken) {
        return GraphServiceClient.builder().authenticationProvider(new MsalGraphAuthenticationProvider(accessToken))
                .buildClient();
    }

    /**
     * Our Msal Graph Authentication Provider class. Required for setting up a
     * GraphServiceClient. It implements ICoreAuthenticationProvider and IAuthenticationProvider.
     */
    private static class MsalGraphAuthenticationProvider
            implements ICoreAuthenticationProvider, IAuthenticationProvider {

        private static final String AUTHORIZATION_HEADER = "Authorization";
        private static final String BEARER = "Bearer";
        private String authTokenHeaderValue;

        /**
         * Set up the MsalGraphAuthenticationProvider. Put `Bearer <token>` into
         * authTokenHeaderValue (used by the authenticateRequest methods)
         * 
         * @param accessToken your access token for Graph
         */
        public MsalGraphAuthenticationProvider(String accessToken) {
            authTokenHeaderValue = String.format("%s %s", BEARER, accessToken);
        }

        /**
         * This implementation of the ICoreAuthenticationProvider injects the Graph
         * access token from Azure AD into the headers of the okhttp3.Request used by
         * GraphSDK/GraphSDKCore. This is the new format that the GraphSDK will use
         * going forward.
         */
        @Override
        public Request authenticateRequest(Request request) {
            return request.newBuilder().addHeader(AUTHORIZATION_HEADER, authTokenHeaderValue).build();
        }

        /**
         * This implementation of the IAuthenticationProvider injects the Graph access
         * token from Azure AD into the headers of the IHttp request used by GraphSDK.
         * This is used in the GraphSDK but with a deprecation warning.
         */
        @Override
        public void authenticateRequest(IHttpRequest request) {
            request.addHeader(AUTHORIZATION_HEADER, authTokenHeaderValue);
        }
    }

    /**
     * Get groups that the user belongs to from MS Graph
     */
    public static List<Group> getGroups(IGraphServiceClient graphClient) {
        // Set up the initial request builder and build request for the first page
        IGroupCollectionRequestBuilder groupsRequestBuilder = graphClient.groups();
        IGroupCollectionRequest groupsRequest = groupsRequestBuilder.buildRequest().top(999);

        List<Group> allGroups = new ArrayList<>();

        do {
            try {
                // Execute the request
                IGroupCollectionPage groupsCollection = groupsRequest.get();

                // Process each of the items in the response
                for (Group group : groupsCollection.getCurrentPage()) {
                    allGroups.add(group);
                }

                // Build the request for the next page, if there is one
                groupsRequestBuilder = groupsCollection.getNextPage();
                if (groupsRequestBuilder == null) {
                    groupsRequest = null;
                } else {
                    groupsRequest = groupsRequestBuilder.buildRequest();
                }

            } catch (ClientException ex) {
                // Handle failure
                logger.log(Level.WARNING, ex.getMessage());
                logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
                groupsRequest = null;
            }

        } while (groupsRequest != null);

        return allGroups;

    }
}
