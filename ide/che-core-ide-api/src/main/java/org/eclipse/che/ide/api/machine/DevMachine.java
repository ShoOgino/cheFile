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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

/**
 * Describe development machine instance.
 * Must contains all information that need to communicate with dev machine such as links, type, environment variable and etc.
 *
 * @author Vitalii Parfonov
 */
public class DevMachine extends MachineEntityImpl {

    public DevMachine(String name, @NotNull Machine devMachineDescriptor) {
        super(name, devMachineDescriptor);
    }

    public String getWsAgentWebSocketUrl() {
        for (Link link : machineLinks) {
            if (Constants.WSAGENT_WEBSOCKET_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        final String message = "Reference " + Constants.WSAGENT_WEBSOCKET_REFERENCE + " not found in DevMachine description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    /**
     *
     * @return return base URL to the ws agent REST services. URL will be always without trailing slash
     */
    public String getWsAgentBaseUrl() {
        MachineServer server = getServer(Constants.WSAGENT_REFERENCE);
        if (server != null) {
            String url = server.getUrl();
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            return url + "/api";
        } else {
            //should not be
            String message = "Reference " + Constants.WSAGENT_REFERENCE + " not found in DevMachine description";
            Log.error(getClass(), message);
            throw new RuntimeException(message);
        }
    }
}
