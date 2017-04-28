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
package org.eclipse.che.wsagent.server;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/**
 * Provides URL to workspace agent inside container.
 *
 * @author Anton Korneta
 */
public class WsAgentURLProvider implements Provider<String> {
    private static final Logger LOG = LoggerFactory.getLogger(WsAgentURLProvider.class);

    private final String                 wsId;
    private final String                 workspaceApiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    private String cachedAgentUrl;

    @Inject
    public WsAgentURLProvider(@Named("che.api") String apiEndpoint,
                              @Named("env.CHE_WORKSPACE_ID") String wsId,
                              HttpJsonRequestFactory requestFactory) {
        this.wsId = wsId;
        this.workspaceApiEndpoint = apiEndpoint + "/workspace/";
        this.requestFactory = requestFactory;
    }

    @Override
    public String get() {
        if (isNullOrEmpty(cachedAgentUrl)) {
            try {
                final WorkspaceDto workspace = requestFactory.fromUrl(workspaceApiEndpoint + wsId)
                                                             .useGetMethod()
                                                             .request()
                                                             .asDto(WorkspaceDto.class);
                if (workspace.getRuntime() != null) {
                    MachineDto machineDto = workspace.getRuntime().getMachines().get("dev-machine");
                    for (Map.Entry<String, ServerDto> serverEntry : machineDto.getServers().entrySet()) {
                        if (WSAGENT_REFERENCE.equals(serverEntry.getKey())) {
                            cachedAgentUrl = serverEntry.getValue().getUrl();
                            return cachedAgentUrl;
                        }
                    }
                }
            } catch (ApiException | IOException ex) {
                LOG.warn(ex.getLocalizedMessage());
                throw new RuntimeException("Failed to configure wsagent endpoint");
            }
        }
        return cachedAgentUrl;
    }
}
