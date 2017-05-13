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
package org.eclipse.che.api.workspace.server.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Starting point of describing the contract which infrastructure provider should implement
 * for making infrastructure suitable for serving workspace runtimes.
 *
 * @author gazarenkov
 */
public abstract class RuntimeInfrastructure {

    private static final Logger LOG = getLogger(RuntimeInfrastructure.class);

    protected final Set<String> recipeTypes;
    protected final String      name;
    protected final URL masterChannel;

    public RuntimeInfrastructure(String name, Collection<String> types, String masterChannel) {
        Preconditions.checkArgument(!types.isEmpty());
        this.name = Objects.requireNonNull(name);
        this.recipeTypes = ImmutableSet.copyOf(types);
        URL tmp;
        try {
            tmp = new URL(masterChannel);
        } catch (MalformedURLException e) {
            tmp = null;
            LOG.error("URL building error for RuntimeInfrastructure master channel: " + name + " : " +  e.getMessage());
        }
        this.masterChannel = tmp;
    }

    /**
     * Returns the name of this runtime infrastructure.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the types of the recipes supported by this runtime infrastructure.
     * The set is never empty and contains at least one recipe type.
     */
    public final Set<String> getRecipeTypes() {
        return recipeTypes;
    }


    /**
     * @return master channel
     */
    public URL getMasterChannel() {
        return masterChannel;
    }

    /**
     * An Infrastructure implementation should be able to preliminary estimate incoming Environment.
     * For example: for validating it before storing
     * The method SHOULD validate Environment. If it is valid, an Infrastructure MAY return more fine grained Environment
     * For example:
     * - if Machines are not described this method may add machine descriptions calculated against Recipe
     * - implementation may add additional Attributes based on incoming Recipe
     *
     * @param environment
     *         incoming Environment to estimate
     * @return calculated environment if any or same environment as incoming or null.
     * In all of this cases Environment is taken as valid.
     * @throws ValidationException
     *         if incoming Environment is not valid
     * @throws InfrastructureException
     *         if any other error occurred
     */
    public abstract Environment estimate(Environment environment) throws ValidationException, InfrastructureException;

    /**
     * An Infrastructure MAY track Runtimes. In this case the method should be overridden.
     *
     * One of the reason for infrastructure to support this is ability to recover infrastructure
     * after shutting down Master server. For this purpose an Infrastructure should also implement
     * getRuntime(id) method
     *
     * @return list of tracked Runtimes' Identities.
     * @throws UnsupportedOperationException
     *         if implementation does not support runtimes tracking
     * @throws InfrastructureException
     *         if any other error occurred
     */
    public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
        throw new UnsupportedOperationException("The implementation does not track runtimes");
    }

    /**
     * An Infrastructure MAY track Runtimes. In this case the method should be overridden.
     *
     * One of the reason for infrastructure to support this is ability to recover infrastructure
     * after shutting down Master server.
     *
     * @param id
     *         the RuntimeIdentity
     * @return the Runtime
     * @throws UnsupportedOperationException
     *         if implementation does not support runtimes tracking
     * @throws InfrastructureException
     *         if any other error occurred
     */
    public InternalRuntime getRuntime(RuntimeIdentity id) throws InfrastructureException {
        throw new UnsupportedOperationException("The implementation does not track runtimes");
    }

    /**
     * Making Runtime is a two phase process.
     * On the first phase implementation MUST prepare RuntimeContext, this is supposedly "fast" method
     * On the second phase Runtime is created with RuntimeContext.start() which is supposedly "long" method
     *
     * @param id
     *         the RuntimeIdentity
     * @param environment
     *         incoming Environment (configuration)
     * @return new RuntimeContext object
     * @throws ValidationException
     *         if incoming environment is not valid
     * @throws InfrastructureException
     *         if any other error occurred
     */
    public abstract RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException, InfrastructureException;


}
