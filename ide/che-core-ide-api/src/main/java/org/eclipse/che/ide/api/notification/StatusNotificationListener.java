/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.notification;

/**
 * Listener interface for being notified about changes of notification status.
 *
 * @author Roman Nikitenko
 */
public interface StatusNotificationListener {
  /** Performs some actions in response to a notification status has been changed */
  void onNotificationStatusChanged(StatusNotification notification);
}
