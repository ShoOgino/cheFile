/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.server;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


@Singleton
public class KeycloakHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    @Inject
    public KeycloakHttpJsonRequestFactory() {
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        System.out.println(" setAuthorizationHeader for " + url);
        return super.fromUrl(url).setAuthorizationHeader("Internal");
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        System.out.println(" setAuthorizationHeader for " + link);       
        return super.fromLink(link).setAuthorizationHeader("Internal");
    }

}
