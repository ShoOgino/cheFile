/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.actions;

/** Factory to create menu for adding new tab */
public interface AddTabMenuFactory {

  /**
   * Creates new menu for adding new tab in processes panel.
   *
   * @return menu
   */
  AddTabMenu newAddTabMenu();
}
