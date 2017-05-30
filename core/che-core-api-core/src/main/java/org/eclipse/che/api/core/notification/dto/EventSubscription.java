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
package org.eclipse.che.api.core.notification.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

@DTO
public interface EventSubscription {
    String getMethod();

    EventSubscription withMethod(String method);

    Map<String, String> getScope();

    EventSubscription withScope(Map<String, String> scope);
}
