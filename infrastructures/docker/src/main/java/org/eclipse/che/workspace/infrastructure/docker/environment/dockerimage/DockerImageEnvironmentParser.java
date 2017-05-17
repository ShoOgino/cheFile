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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

/**
 * Docker image specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerImageEnvironmentParser implements DockerConfigSourceSpecificEnvironmentParser {

    @Override
    public DockerEnvironment parse(Environment environment) throws ValidationException {
        Recipe recipe = environment.getRecipe();

        if (!"dockerimage".equals(recipe.getType())) {
            throw new ValidationException(format("Docker image environment parser doesn't support recipe type '%s'",
                                                      recipe.getType()));
        }

        DockerEnvironment dockerEnv = new DockerEnvironment();
        DockerContainerConfig service = new DockerContainerConfig();
        dockerEnv.getServices().put(getMachineName(environment), service);

        service.setImage(recipe.getLocation());

        return dockerEnv;
    }

    private String getMachineName(Environment environment) throws ValidationException {
        checkArgument(environment.getMachines().size() == 1,
                      "Environment of type '%s' doesn't support multiple machines, but contains machines: %s",
                      environment.getRecipe().getType(),
                      Joiner.on(", ").join(environment.getMachines().keySet()));

        return environment.getMachines()
                          .keySet()
                          .iterator()
                          .next();
    }
}
