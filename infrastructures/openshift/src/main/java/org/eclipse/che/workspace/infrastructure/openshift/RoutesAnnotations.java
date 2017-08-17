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
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.openshift.api.model.Route;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helps to convert {@link Route} related OpenShift infrastructure entities
 * to annotations and vise-versa.
 *
 * @author Sergii Leshchenko
 */
public class RoutesAnnotations {
    public static final String ANNOTATION_PREFIX = "org.eclipse.che.";

    public static final String SERVER_PORT_ANNOTATION_FMT     = ANNOTATION_PREFIX + "server.%s.port";
    public static final String SERVER_PROTOCOL_ANNOTATION_FMT = ANNOTATION_PREFIX + "server.%s.protocol";
    public static final String SERVER_PATH_ANNOTATION_FMT     = ANNOTATION_PREFIX + "server.%s.path";

    /** Pattern that matches server annotations e.g. "org.eclipse.che.server.exec-agent.port". */
    private static final Pattern SERVER_ANNOTATION_PATTERN = Pattern.compile("org\\.eclipse\\.che\\.server\\.(?<ref>[\\w-/]+)\\..+");

    /** Creates new annotations serializer. */
    public static Serializer newSerializer() { return new Serializer(); }

    /** Creates new label deserializer from given annotations. */
    public static Deserializer newDeserializer(Map<String, String> annotations) { return new Deserializer(annotations); }

    /** Helps to serialize known route related entities to OpenShift annotations. */
    public static class Serializer {
        private final Map<String, String> annotations = new LinkedHashMap<>();

        /**
         * Serializes server configuration as OpenShift Route annotations.
         * Appends serialization result to this aggregate.
         *
         * @param ref
         *         server reference e.g. "exec-agent"
         * @param server
         *         server configuration
         * @return this serializer
         */
        public Serializer server(String ref, ServerConfig server) {
            annotations.put(String.format(SERVER_PORT_ANNOTATION_FMT, ref), server.getPort());
            annotations.put(String.format(SERVER_PROTOCOL_ANNOTATION_FMT, ref), server.getProtocol());
            if (server.getPath() != null) {
                annotations.put(String.format(SERVER_PATH_ANNOTATION_FMT, ref), server.getPath());
            }
            return this;
        }

        public Serializer servers(Map<String, ? extends ServerConfig> servers) {
            servers.forEach(this::server);
            return this;
        }

        public Map<String, String> annotations() {
            return annotations;
        }
    }

    /** Helps to deserialize OpenShift annotations to known route related entities. */
    public static class Deserializer {
        private final Map<String, String> annotations;

        public Deserializer(Map<String, String> annotations) { this.annotations = Objects.requireNonNull(annotations); }

        /** Retrieves server configuration from route annotations and returns (ref -> server config) map. */
        public Map<String, ServerConfig> servers() {
            Map<String, ServerConfig> servers = new HashMap<>();
            for (Map.Entry<String, String> entry : annotations.entrySet()) {
                Matcher refMatcher = SERVER_ANNOTATION_PATTERN.matcher(entry.getKey());
                if (refMatcher.matches()) {
                    String ref = refMatcher.group("ref");
                    if (!servers.containsKey(ref)) {
                        servers.put(ref, new ServerConfigImpl(annotations.get(String.format(SERVER_PORT_ANNOTATION_FMT, ref)),
                                                              annotations.get(String.format(SERVER_PROTOCOL_ANNOTATION_FMT, ref)),
                                                              annotations.get(String.format(SERVER_PATH_ANNOTATION_FMT, ref))));
                    }
                }
            }
            return servers;
        }
    }

    private RoutesAnnotations() {}
}
