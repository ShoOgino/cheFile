/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.snapshot;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/** @author Alexander Garagatyi */
public class SnapshotException extends ServerException {
  public SnapshotException(String message) {
    super(message);
  }

  public SnapshotException(ServiceError serviceError) {
    super(serviceError);
  }

  public SnapshotException(Throwable cause) {
    super(cause);
  }

  public SnapshotException(String message, Throwable cause) {
    super(message, cause);
  }
}
