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
package org.eclipse.che.api.deploy.jsonrpc;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.execution.ExecutorServiceProvider;

/** {@link ExecutorService} provider used in {@link CheMajorWebSocketEndpoint}. */
@Singleton
public class CheMajorWebSocketEndpointExecutorServiceProvider extends ExecutorServiceProvider {

  public static final String JSON_RPC_MAJOR_CORE_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.processor_core_pool_size";
  public static final String JSON_RPC_MAJOR_MAX_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.processor_max_pool_size";
  public static final String JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME =
      "che.core.jsonrpc.processor_queue_capacity";

  @Inject
  public CheMajorWebSocketEndpointExecutorServiceProvider(
      @Named(JSON_RPC_MAJOR_CORE_POOL_SIZE_PARAMETER_NAME) int corePoolSize,
      @Named(JSON_RPC_MAJOR_MAX_POOL_SIZE_PARAMETER_NAME) int maxPoolSize,
      @Named(JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME) int queueCapacity) {
    super(corePoolSize, maxPoolSize, queueCapacity);
  }
}
