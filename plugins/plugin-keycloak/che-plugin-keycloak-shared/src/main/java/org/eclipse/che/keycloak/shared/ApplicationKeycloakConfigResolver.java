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
package org.eclipse.che.keycloak.shared;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * Config resolver for public client applications.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class ApplicationKeycloakConfigResolver extends AbstractKeycloakConfigResolver {
    @Override
    protected AdapterConfig prepareConfig() {
        AdapterConfig config = new AdapterConfig();
        config.setSslRequired(SslRequired.EXTERNAL.toString().toLowerCase());
        config.setCors(true);
        config.setBearerOnly(false);
        config.setPublicClient(true);
        config.setConnectionPoolSize(20);
        config.setDisableTrustManager(true);
        return config;
    }
}
