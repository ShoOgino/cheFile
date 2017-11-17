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
package org.eclipse.che.ide;

import com.google.gwt.user.client.Window.Location;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides access to query parameters
 *
 * @author Dmitry Shnurenko
 * @author Sergii Leschenko
 */
@Singleton
public class QueryParameters {

  private final Map<String, List<String>> cachedParameters;

  @Inject
  public QueryParameters() {
    // cache the query parameters because IDE changes window's location
    cachedParameters = Location.getParameterMap();
  }

  /**
   * Returns the query parameter by the specified name or empty string if parameter was not found.
   * Note that if multiple parameters have been specified with the same name, the last one will be
   * returned.
   *
   * @param name name of value parameter
   * @return query parameter value
   */
  public String getByName(String name) {
    List<String> paramsForName = cachedParameters.get(name);

    if (paramsForName == null) {
      return "";
    } else {
      return paramsForName.get(paramsForName.size() - 1);
    }
  }

  /**
   * Returns map containing key and value of query parameters or empty map if there are no
   * parameters.
   *
   * <p>Note that if multiple parameters have been specified with the same name, the result will
   * contains only first one.
   *
   * @return map with query parameters
   */
  public Map<String, String> getAll() {
    Map<String, String> parameters = new HashMap<>();
    for (Map.Entry<String, List<String>> parametersEntry : cachedParameters.entrySet()) {
      parameters.put(parametersEntry.getKey(), parametersEntry.getValue().get(0));
    }
    return parameters;
  }
}
