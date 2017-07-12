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
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;

import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;

@Singleton
class InstallerLogHandler {

    @Inject
    InstallerLogHandler(RequestHandlerConfigurator configurator, EventBus eventBus) {
        configurator.newConfiguration()
                    .methodName(INSTALLER_LOG_METHOD)
                    .paramsAsDto(InstallerLogEvent.class)
                    .noResult()
                    .withBiConsumer((endpointId, log) -> eventBus.fireEvent(new EnvironmentOutputEvent(log.getText(),
                                                                                                       log.getMachineName())));
    }
}
