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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import javax.sql.DataSource;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class MultiUserCheWsMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.JndiDataSourceProvider.class);
  }
}
