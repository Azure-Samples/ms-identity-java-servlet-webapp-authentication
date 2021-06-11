// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azuresamples.msal4j.helpers;

import java.io.IOException;

/**
 * Implement this so that AuthHelper can be customized to your needs!
 * This Sample project implements this in IdentityContextAdapterServlet.java
 * MUST BE INSTANTIATED ONCE PER REQUEST IN WEB APPS / WEB APIs before passing to AuthHelper
 */
/* why this called adapter ? is it reference to adapter structural design pattern
 (allows objects with incompatible interfaces to collaborate)*

 not comments to induvidual methods,  not clear what getParameter is supposed to do

 not sure that I understand intenion of this interface
 */
public interface IdentityContextAdapter {
    public void setContext(IdentityContextData context);
    public IdentityContextData getContext();
    public void redirectUser(String location) throws IOException;
    public String getParameter(String parameterName);
}
