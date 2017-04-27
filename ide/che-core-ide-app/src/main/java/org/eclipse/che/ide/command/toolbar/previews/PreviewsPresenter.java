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
package org.eclipse.che.ide.command.toolbar.previews;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.ProcessStartedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/** Drives the UI for displaying preview URLs of the running processes. */
@Singleton
public class PreviewsPresenter implements Presenter, PreviewsView.ActionDelegate {

    private final PreviewsView             view;
    private final ExecAgentCommandManager  execAgentClient;
    private final CommandManager           commandManager;
    private final AppContext               appContext;
    private final Provider<MacroProcessor> macroProcessorProvider;
    private final PromiseProvider          promiseProvider;
    private final ToolbarMessages          messages;

    @Inject
    public PreviewsPresenter(PreviewsView view,
                             ExecAgentCommandManager execAgentClient,
                             CommandManager commandManager,
                             AppContext appContext,
                             EventBus eventBus,
                             Provider<MacroProcessor> macroProcessorProvider,
                             PromiseProvider promiseProvider,
                             ToolbarMessages messages) {
        this.view = view;
        this.execAgentClient = execAgentClient;
        this.commandManager = commandManager;
        this.appContext = appContext;
        this.macroProcessorProvider = macroProcessorProvider;
        this.promiseProvider = promiseProvider;
        this.messages = messages;

        view.setDelegate(this);

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                updateView();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                view.removeAllURLs();
            }
        });

        eventBus.addHandler(ProcessStartedEvent.TYPE, event -> updateView());
        eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> updateView());
    }

    /** Updates view with the preview URLs of running processes. */
    private void updateView() {
        view.removeAllURLs();

        final WorkspaceRuntime runtime = appContext.getActiveRuntime();

        if (runtime != null) {
            runtime.getMachines().forEach(machine -> {
                Promise<List<GetProcessesResponseDto>> machineProcesses = execAgentClient.getProcesses(machine.getId(), false);
                machineProcesses.then(processes -> {
                    processes.forEach(process -> getPreviewUrl(process).then(view::addUrl).catchError(ignore -> {
                    }));
                });
            });
        }
    }

    /**
     * Returns promise that resolves preview URL for the given process.
     * Returns promise that rejects with an error if preview URL isn't available.
     */
    private Promise<String> getPreviewUrl(GetProcessesResponseDto process) {
        final String previewUrl = commandManager.getCommand(process.getName())
                                                .map(CommandImpl::getPreviewURL)
                                                .orElse(null);

        if (!isNullOrEmpty(previewUrl)) {
            return macroProcessorProvider.get().expandMacros(previewUrl);
        }

        return promiseProvider.reject(new Exception(messages.previewsNotAvailableError()));
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onUrlChosen(String previewUrl) {
        Window.open(previewUrl, "_blank", null);
    }
}
