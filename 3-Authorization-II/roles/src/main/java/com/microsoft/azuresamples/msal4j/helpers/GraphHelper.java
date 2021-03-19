// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.helpers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.microsoft.graph.authentication.BaseAuthenticationProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.GroupCollectionRequest;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.models.Group;

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
     * @param accessToken String your access token for Graph
     * @return GraphServiceClient IGraphServiceClient
     */
    public static GraphServiceClient getGraphClient(String accessToken) {
        return GraphServiceClient.builder().authenticationProvider(new MsalGraphAuthenticationProvider(accessToken))
                .buildClient();
    }

    /**
     * Our Msal Graph Authentication Provider class. Required for setting up a
     * GraphServiceClient. It extends BaseAuthenticationProvider which in turn implements IAuthenticationProvider.
     */
    private static class MsalGraphAuthenticationProvider
            extends BaseAuthenticationProvider {

        private String accessToken;

        /**
         * Set up the MsalGraphAuthenticationProvider. Allows accessToken to be
         * used by GraphServiceClient through the interface IAuthenticationProvider
         * 
         * Make sure you either:
         * 1) get a fresh, valid access token to put here, OR
         * 2) make your own implementation where you will call MSAL's acquireTokenSilently (from getAuthorizationTokenAsync) get a valid token per request
         * 
         * @param accessToken String your access token for Graph
         */
        public MsalGraphAuthenticationProvider(@Nonnull String accessToken) {
           this.accessToken = accessToken;
        }

        /**
         * This implementation of the IAuthenticationProvider injects the Graph access
         * token from Azure AD into the headers of the IHttp request used by GraphSDK.
         * 
         * 1) get a fresh, valid access token to put here, OR
         * 2) make your own implementation where you will call MSAL's acquireTokenSilently to get a valid token per request
         *
         * @param requestUrl the outgoing request URL
         * @return a future with the token
         */
        @Override
        public CompletableFuture<String> getAuthorizationTokenAsync(@Nonnull final URL requestUrl) {
            return CompletableFuture.completedFuture(accessToken);
        }
    }

    /**
     * Get groups that the user belongs to from MS Graph
     */
    public static List<Group> getGroups(GraphServiceClient graphClient) {
        // Set up the initial request builder and build request for the first page
        GroupCollectionRequestBuilder groupsRequestBuilder = graphClient.groups();
        GroupCollectionRequest groupsRequest = groupsRequestBuilder.buildRequest().top(999);

        List<Group> allGroups = new ArrayList<>();

        do {
            try {
                // Execute the request
                GroupCollectionPage groupsCollection = groupsRequest.get();

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
