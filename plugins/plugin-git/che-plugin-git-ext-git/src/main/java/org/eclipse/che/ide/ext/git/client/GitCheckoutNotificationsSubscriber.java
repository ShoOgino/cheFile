/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

/** Subscribes on receiving notifications about changing the current git branch. */
@Singleton
public class GitCheckoutNotificationsSubscriber {

    private final EventBus           eventBus;
    private final AppContext         appContext;
    private final RequestTransmitter requestTransmitter;

    @Inject
    public GitCheckoutNotificationsSubscriber(EventBus eventBus, AppContext appContext, RequestTransmitter requestTransmitter) {
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.requestTransmitter = requestTransmitter;
    }

    void initialize() {
        eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> subscribe());

        // in case ws-agent is already running
        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, event -> {
            if (appContext.getWorkspace().getStatus() == RUNNING) {
                subscribe();
            }
        });
    }

    private void subscribe() {
        requestTransmitter.newRequest()
                          .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                          .methodName("track/git-checkout")
                          .noParams()
                          .sendAndSkipResult();

        // TODO (spi ide)
        initializeGitChangeWatcher();
        initializeGitIndexWatcher();
    }

    private void initializeGitChangeWatcher() {
        requestTransmitter.newRequest()
                          .endpointId("ws-agent")
                          .methodName("track/git-change")
                          .noParams()
                          .sendAndSkipResult();
    }

    private void initializeGitIndexWatcher() {
        requestTransmitter.newRequest()
                          .endpointId("ws-agent")
                          .methodName("track/git-index")
                          .noParams()
                          .sendAndSkipResult();
    }
}
