# Deploy your Python applications to Azure Cloud and use Azure App Service to manage your operations

 1. [Overview](#overview)
 1. [Scenario](#scenario)
 1. [Prerequisites](#prerequisites)
 1. [Setup](#setup)
 1. [Registration](#registration)
 1. [Deployment](#deployment)
 1. [Explore the sample](#explore-the-sample)
 1. [More information](#more-information)
 1. [Community Help and Support](#community-help-and-support)
 1. [Contributing](#contributing)
 1. [Code of Conduct](#code-of-conduct)

## Overview

This sample demonstrates how to deploy a Python Flask web application to **Azure Cloud** using [Azure App Service](https://docs.microsoft.com/azure/app-service/). To do so, we will use the [code sample from flask webapp my tenant authentication](https://github.com/azure-samples/ms-identity-python-flask-webapp-authentication).

## Scenario

- This web application uses [Microsoft Authentication Library \(MSAL\) for Python](https://github.com/AzureAD/microsoft-authentication-library-for-python) (leveraging the OpenID Connect protocol) to sign in a user and obtain a JWT **ID Token** from **Azure AD**.
- Users can only sign in with work or school accounts that are in this application's Azure AD tenant.
- The **ID Token** contains claims that are to verify the user's identity and access rights.

![Overview](./ReadmeFiles/sign-in.png)

## Prerequisites

- An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/documentation/articles/active-directory-howto-tenant/)
- A [user account](https://docs.microsoft.com/en-us/azure/active-directory/fundamentals/add-users-azure-active-directory) in your **Azure AD** tenant.
- [Visual Studio Code](https://code.visualstudio.com/download) is recommended for running and editing this sample.
- [VS Code Azure Tools Extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) extension is recommended for interacting with **Azure** through VS Code interface.
- An **Azure subscription**. This sample uses the free tier of **Azure App Service**.

Recommended, though not strictly necessary if not running the sample locally as well:

- [Python 3.8](https://www.python.org/downloads/)
- A virtual environment to install packages listed in [requirements.txt](requirements.txt)

## Setup

Follow the setup instructions in [flask webapp authentication (my tenant)](https://github.com/azure-samples/ms-identity-python-flask-webapp-authentication) sample.

## Registration

### Register the web app 

Use the same app registration credentials that you used when completing the [flask webapp my tenant authentication](https://github.com/azure-samples/ms-identity-python-flask-webapp-authentication) sample. If you have not completed that sample yet, use the instructions in that sample to proceed.

## Deployment

There just **two** main stages that you will need to complete in order to deploy your project and enable authentication:

1. Upload your project to **Azure App Service** and obtain a published website like `https://example-domain.azurewebsites.net.`
1. Add your published app's redirect URIs to **Azure AD** **App Registration** redirect URIs.

### Deploy the web app (Flask Authentication)

There are various ways to deploy your applications to **Azure App Service**. This sample provides steps for deployment via **VS Code Azure Tools Extension**. For more alternatives, visit: [Static website hosting in Azure Storage](https://docs.microsoft.com/azure/storage/blobs/storage-blob-static-website#uploading-content).

> You may watch the first 3 minutes of this [video tutorial](https://www.youtube.com/watch?v=dNVvFttc-sA) offered by Microsoft Dev Radio for Pycon 2020 in preparation.

#### Step 1: Deploy your app

Follow the instructions in steps 1 through five in the official [Microsoft docs Python deployment tuorial](https://docs.microsoft.com/en-us/azure/developer/python/tutorial-deploy-app-service-on-linux-01).

Work with the [flask webapp authentication (my tenant)](https://github.com/azure-samples/ms-identity-python-flask-webapp-authentication) sample rather than the example sample listed in the document.

#### Step 2: Disable default authentication

Now you need to navigate to the **Azure App Service** Portal, and locate your project there. Once you do, click on the **Authentication/Authorization** blade. There, make sure that the **App Services Authentication** is switched off (and nothing else is checked), as we are using **our own** authentication logic.  

![disable_easy_auth](./ReadmeFiles/disable_easy_auth.png)

#### Step 2: Update the client app's authentication parameters

1. Navigate back to to the [Azure Portal](https://portal.azure.com).
1. In the left-hand navigation pane, select the **Azure Active Directory** service, and then select **App registrations**.
1. In the resulting screen, select the name of your application.
1. From the *Branding* menu, update the **Home page URL**, to the address of your service, for example `https://example.azurewebsites.net/`. Save the configuration.
1. Append the redirect endpoint to this URI and add it to the list of values of the *Authentication -> Redirect URIs* menu. If you have multiple redirect URIs, make sure that there a new entry using the App service's URI for each redirect URI.


## Explore the sample



## We'd love your feedback!

<!-- Were we successful in addressing your learning objective? Consider taking a moment to [share your experience with us](https://forms.office.com/Pages/ResponsePage.aspx?id=v4j5cvGGr0GRqy180BHbR73pcsbpbxNJuZCMKN0lURpUNDVHTkg2VVhWMTNYUTZEM05YS1hSN01EOSQlQCN0PWcu). -->

## More information

- [Azure App Services](https://docs.microsoft.com/azure/app-service/)

For more information about how OAuth 2.0 protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](https://docs.microsoft.com/azure/active-directory/develop/authentication-flows-app-scenarios).

## Community Help and Support

Use [Stack Overflow](http://stackoverflow.com/questions/tagged/msal) to get support from the community.
Ask your questions on Stack Overflow first and browse existing issues to see if someone has asked your question before.
Make sure that your questions or comments are tagged with [`azure-ad` `azure-ad-b2c` `ms-identity` `msal`].

If you find a bug in the sample, please raise the issue on [GitHub Issues](../../issues).

To provide a recommendation, visit the following [User Voice page](https://feedback.azure.com/forums/169401-azure-active-directory).

## Contributing

If you'd like to contribute to this sample, see [CONTRIBUTING.MD](../../CONTRIBUTING.md).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information, see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
