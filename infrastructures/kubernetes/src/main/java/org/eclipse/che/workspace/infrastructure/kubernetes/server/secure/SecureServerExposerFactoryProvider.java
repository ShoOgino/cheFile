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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import static java.lang.String.format;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides implementation of {@link SecureServerExposerFactory} according to configuration property
 * with name `che.server.secure_exposer`.
 *
 * @author Sergii Leshchenko
 * @author Oleksandr Garagatyi
 */
public class SecureServerExposerFactoryProvider<T extends KubernetesEnvironment> {
  private static final String SECURE_EXPOSER_IMPL_PROPERTY = "che.server.secure_exposer";

  private final SecureServerExposerFactory<T> serverExposerFactory;

  @Inject
  public SecureServerExposerFactoryProvider(
      @Named(SECURE_EXPOSER_IMPL_PROPERTY) String serverExposer,
      Map<String, SecureServerExposerFactory<T>> factories) {
    String knownExposers = String.join(", ", factories.keySet());
    serverExposerFactory = factories.get(serverExposer);
    if (serverExposerFactory == null) {
      throw new ConfigurationException(
          format(
              "Unknown secure servers exposer '%s' is configured. Currently supported: %s.",
              serverExposer, knownExposers));
    }
  }

  /**
   * Creates instance of {@link SecureServerExposerFactory} that will expose secure servers of
   * Kubernetes environment for runtime with the specified runtime identity.
   */
  public SecureServerExposerFactory<T> get() {
    return this.serverExposerFactory;
  }
}
