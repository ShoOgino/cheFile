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
package org.eclipse.che.ide.api.workspace.model;

import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;

/** Data object for {@link Runtime}. */
public class RuntimeImpl implements Runtime {

    private final String                   activeEnv;
    private final String                   owner;
    private       Map<String, MachineImpl> machines;
    private       List<Warning>            warnings;

    public RuntimeImpl(String activeEnv,
                       Map<String, ? extends Machine> machines,
                       String owner,
                       List<? extends Warning> warnings) {
        this.activeEnv = activeEnv;
        if (machines != null) {
            this.machines = machines.entrySet()
                                    .stream()
                                    .collect(toMap(Map.Entry::getKey,
                                                   entry -> new MachineImpl(entry.getKey(),
                                                                            entry.getValue().getProperties(),
                                                                            entry.getValue().getServers())));
        }
        this.owner = owner;
        if (warnings != null) {
            this.warnings = warnings.stream()
                                    .map(WarningImpl::new)
                                    .collect(toList());
        }
    }

    @Override
    public String getActiveEnv() {
        return activeEnv;
    }

    @Override
    public Map<String, MachineImpl> getMachines() {
        if (machines == null) {
            machines = new HashMap<>();
        }
        return machines;
    }

    /**
     * Returns a dev-machine or an empty {@code Optional} if none.
     * Dev-machine is a machine where ws-agent server is running.
     */
    public Optional<MachineImpl> getDevMachine() {
        return getMachines().values()
                            .stream()
                            .filter(m -> m.getServerByName(SERVER_WS_AGENT_HTTP_REFERENCE).isPresent())
                            .findAny();
    }

    public Optional<MachineImpl> getMachineByName(String name) {
        return Optional.ofNullable(getMachines().get(name));
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public List<Warning> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuntimeImpl)) return false;
        RuntimeImpl that = (RuntimeImpl)o;
        return Objects.equals(activeEnv, that.activeEnv) &&
               Objects.equals(machines, that.machines) &&
               Objects.equals(owner, that.owner) &&
               Objects.equals(warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeEnv,
                            machines,
                            owner,
                            warnings);
    }

    @Override
    public String toString() {
        return "RuntimeImpl{" +
               "activeEnv='" + activeEnv + '\'' +
               ", machines='" + machines + '\'' +
               ", owner=" + owner +
               ", warnings=" + warnings +
               '}';
    }
}
