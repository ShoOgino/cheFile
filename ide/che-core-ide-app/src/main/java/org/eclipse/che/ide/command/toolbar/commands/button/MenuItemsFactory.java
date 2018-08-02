/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.commands.button;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/** Factory for {@link MenuItem}s for {@link ExecuteCommandButton}. */
public interface MenuItemsFactory {

  GuideItem newGuideItem(CommandGoal goal);

  CommandItem newCommandItem(CommandImpl command);

  MachineItem newMachineItem(CommandImpl command, MachineImpl machine);

  MachineItem newMachineItem(MachineItem item);
}
