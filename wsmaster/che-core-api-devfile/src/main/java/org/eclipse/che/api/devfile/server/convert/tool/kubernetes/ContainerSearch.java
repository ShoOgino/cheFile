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
package org.eclipse.che.api.devfile.server.convert.tool.kubernetes;

import static java.util.stream.Collectors.toList;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplate;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Template;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Container search goes through a list of Kubernetes resources and recursively looks for containers
 * that match the provided criteria.
 *
 * <p>The deployment and pod name criteria work both on the {@code name} and, if name is not set on
 * given deployment/pod, on the {@code generateName} of the objects.
 */
public class ContainerSearch {

  private final @Nullable String parentName;
  private final @Nullable Map<String, String> parentSelector;
  private final @Nullable String containerName;

  /**
   * Constructs a new {@code ContainerSearch} instance in somewhat unsurprising manner.
   *
   * @param parentName the name of the parent object that should (indirectly) contain the containers
   * @param parentSelector the selector for the
   * @param containerName only search for containers with given name
   */
  public ContainerSearch(
      @Nullable String parentName,
      @Nullable Map<String, String> parentSelector,
      @Nullable String containerName) {
    this.parentName = parentName;
    this.parentSelector = parentSelector;
    this.containerName = containerName;
  }

  /**
   * Searches for containers in the provided list of Kubernetes objects. If any given item in the
   * list can contain a container (i.e. it is a pod, deployment, etc.) the item is searched for the
   * containers recursively.
   *
   * @param list the list of Kubernetes resources to sift through
   * @return a list of containers found in the provided object list
   */
  public List<Container> search(Collection<? extends HasMetadata> list) {
    return list.stream()
        .filter(this::matchMeta)
        .flatMap(this::findContainers)
        .filter(this::matchContainer)
        .collect(toList());
  }

  private Stream<Container> findContainers(HasMetadata o) {
    // hopefully, this covers all types of objects that can contain a container
    if (o instanceof Pod) {
      return ((Pod) o).getSpec().getContainers().stream();
    } else if (o instanceof PodTemplate) {
      return ((PodTemplate) o).getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof DaemonSet) {
      return ((DaemonSet) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof Deployment) {
      return ((Deployment) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof Job) {
      return ((Job) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof ReplicaSet) {
      return ((ReplicaSet) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof ReplicationController) {
      return ((ReplicationController) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof StatefulSet) {
      return ((StatefulSet) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof CronJob) {
      return ((CronJob) o)
          .getSpec()
          .getJobTemplate()
          .getSpec()
          .getTemplate()
          .getSpec()
          .getContainers()
          .stream();
    } else if (o instanceof DeploymentConfig) {
      return ((DeploymentConfig) o).getSpec().getTemplate().getSpec().getContainers().stream();
    } else if (o instanceof Template) {
      return ((Template) o).getObjects().stream().flatMap(this::findContainers);
    } else {
      return Stream.empty();
    }
  }

  private boolean matchContainer(Container container) {
    return this.containerName == null || this.containerName.equals(container.getName());
  }

  private boolean matchMeta(HasMetadata object) {
    return matches(object.getMetadata(), parentName, parentSelector);
  }

  private static boolean matches(
      ObjectMeta metaData, @Nullable String name, @Nullable Map<String, String> labels) {
    if (name == null) {
      return labels == null || matchesBySelector(metaData, labels);
    } else {
      boolean ret = matchesByName(metaData, name);
      return labels == null ? ret : ret && matchesBySelector(metaData, labels);
    }
  }

  private static boolean matchesByName(ObjectMeta metaData, String name) {
    if (name == null) {
      return true;
    }

    String metaName = metaData == null ? null : metaData.getName();
    String metaGenerateName = metaData == null ? null : metaData.getGenerateName();

    // do not compare by the generateName if a name exists
    if (metaName != null) {
      return name.equals(metaName);
    } else {
      return name.equals(metaGenerateName);
    }
  }

  private static boolean matchesBySelector(ObjectMeta metaData, Map<String, String> labels) {
    return SelectorFilter.test(metaData, labels);
  }
}
