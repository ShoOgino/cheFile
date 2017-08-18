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
package org.eclipse.che.api.workspace.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface ServerConfigDto extends ServerConfig {
  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getPort();

  void setPort(String port);

  ServerConfigDto withPort(String port);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getProtocol();

  void setProtocol(String protocol);

  ServerConfigDto withProtocol(String protocol);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getPath();

  void setPath(String path);

  ServerConfigDto withPath(String path);
}
