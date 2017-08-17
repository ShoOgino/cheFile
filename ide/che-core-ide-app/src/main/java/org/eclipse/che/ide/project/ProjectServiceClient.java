/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.project;

import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.UrlBuilder;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static com.google.gwt.safehtml.shared.UriUtils.encodeAllowEscapes;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Serves the connections with the server side project service.
 * By design this service is laid on the lowest business level which is operating only with data transfer objects (DTO).
 * This interface is not intended to implementing by the third party components or using it directly.
 */
public class ProjectServiceClient {

    private static final String PROJECT        = "/project";
    private static final String BATCH_PROJECTS = "/batch";

    private static final String ITEM     = "/item";
    private static final String TREE     = "/tree";
    private static final String MOVE     = "/move";
    private static final String COPY     = "/copy";
    private static final String FOLDER   = "/folder";
    private static final String FILE     = "/file";
    private static final String SEARCH   = "/search";
    private static final String IMPORT   = "/import";
    private static final String RESOLVE  = "/resolve";
    private static final String ESTIMATE = "/estimate";

    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    reqFactory;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory unmarshaller;
    private final AppContext             appContext;

    @Inject
    ProjectServiceClient(LoaderFactory loaderFactory,
                         AsyncRequestFactory reqFactory,
                         DtoFactory dtoFactory,
                         DtoUnmarshallerFactory unmarshaller,
                         AppContext appContext) {
        this.loaderFactory = loaderFactory;
        this.reqFactory = reqFactory;
        this.dtoFactory = dtoFactory;
        this.unmarshaller = unmarshaller;
        this.appContext = appContext;
    }

    /**
     * Returns the projects list. If there is no projects were found on server, empty list is returned.
     *
     * @return {@link Promise} with list of project configuration
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    public Promise<List<ProjectConfigDto>> getProjects() {
        final String url = getBaseUrl();

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting projects..."))
                         .send(unmarshaller.newListUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Estimates the given {@code path} to be applied to specified {@code pType} (project type).
     *
     * @param path
     *         path to the folder
     * @param pType
     *         project type to estimate
     * @return {@link Promise} with the {@link SourceEstimation}
     * @see Path
     * @see SourceEstimation
     * @since 4.4.0
     */
    public Promise<SourceEstimation> estimate(Path path, String pType) {
        final String url = encodeAllowEscapes(getBaseUrl() + ESTIMATE + path(path.toString()) + "?type=" + pType);

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Estimating project..."))
                         .send(unmarshaller.newUnmarshaller(SourceEstimation.class));
    }

    /**
     * Gets list of {@link SourceEstimation} for all supposed project types.
     *
     * @param path
     *         path of the project to resolve
     * @return {@link Promise} with the list of resolved estimations
     * @see Path
     * @see SourceEstimation
     * @since 4.4.0
     */
    public Promise<List<SourceEstimation>> resolveSources(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + RESOLVE + path(path.toString()));

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Resolving sources..."))
                         .send(unmarshaller.newListUnmarshaller(SourceEstimation.class));
    }

    /**
     * Imports the new project by given {@code source} configuration.
     *
     * @param path
     *         path to the future project
     * @param source
     *         source configuration
     * @return a promise that will resolve when the project has been imported, or rejects with an error
     * @see Path
     * @see SourceStorageDto
     * @since 4.4.0
     */
    public Promise<Void> importProject(Path path, SourceStorageDto source) {
        String url = encodeAllowEscapes(getBaseUrl() + IMPORT + path(path.toString()));

        return reqFactory.createPostRequest(url, source)
                         .header(CONTENT_TYPE, APPLICATION_JSON)
                         .send();
    }

    /**
     * Searches an item(s) with the specified criteria given by {@code expression}.
     *
     * @param expression
     *         search query expression
     * @return {@link Promise} with the list of found items
     * @see QueryExpression
     * @see ItemReference
     * @since 4.4.0
     */
    public Promise<List<SearchResult>> search(QueryExpression expression) {
        final String url =
                encodeAllowEscapes(getBaseUrl() + SEARCH + (isNullOrEmpty(expression.getPath()) ? Path.ROOT : path(expression.getPath())));

        StringBuilder queryParameters = new StringBuilder();
        if (expression.getName() != null && !expression.getName().isEmpty()) {
            queryParameters.append("&name=").append(expression.getName());
        }
        if (expression.getText() != null && !expression.getText().isEmpty()) {
            queryParameters.append("&text=").append(expression.getText());
        }
        if (expression.getMaxItems() != 0) {
            queryParameters.append("&maxItems=").append(expression.getMaxItems());
        }
        if (expression.getSkipCount() != 0) {
            queryParameters.append("&skipCount=").append(expression.getSkipCount());
        }

        return reqFactory.createGetRequest(url + queryParameters.toString().replaceFirst("&", "?"))
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Searching..."))
                         .send(unmarshaller.newListUnmarshaller(SearchResultDto.class)).then(
                        (Function<List<SearchResultDto>, List<SearchResult>>)searchResultDtos -> {
                            if (searchResultDtos.isEmpty()) {
                                return Collections.emptyList();
                            }
                            return searchResultDtos.stream().map(SearchResult::new).collect(Collectors.toList());
                        });
    }

    /**
     * Creates the project with given {@code configuration}.
     *
     * @param configuration
     *         the project configuration
     * @param options
     *         additional parameters that need for project generation
     * @return {@link Promise} with the {@link ProjectConfigDto}
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    public Promise<ProjectConfigDto> createProject(ProjectConfigDto configuration, Map<String, String> options) {
        UrlBuilder urlBuilder = new UrlBuilder(getBaseUrl());
        for (String key : options.keySet()) {
            urlBuilder.setParameter(key, options.get(key));
        }
        return reqFactory.createPostRequest(urlBuilder.buildString(), configuration)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Creating project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Create batch of projects according to their configurations.
     * <p/>
     * Notes: a project will be created by importing when project configuration contains {@link SourceStorageDto}
     * object, otherwise this one will be created corresponding its {@link NewProjectConfigDto}:
     * <li> - {@link NewProjectConfigDto} object contains only one mandatory {@link NewProjectConfigDto#setPath(String)} field.
     * In this case Project will be created as project of "blank" type </li>
     * <li> - a project will be created as project of "blank" type when declared primary project type is not registered, </li>
     * <li> - a project will be created without mixin project type when declared mixin project type is not registered</li>
     * <li> - for creating a project by generator {@link NewProjectConfigDto#getOptions()} should be specified.</li>
     *
     * @param configurations
     *         the list of configurations to creating projects
     * @return {@link Promise} with the list of {@link ProjectConfigDto}
     * @see ProjectConfigDto
     */
    public Promise<List<ProjectConfigDto>> createBatchProjects(List<NewProjectConfigDto> configurations) {
        final String url = encodeAllowEscapes(getBaseUrl() + BATCH_PROJECTS);
        final String loaderMessage = configurations.size() > 1 ? "Creating the batch of projects..." : "Creating project...";
        return reqFactory.createPostRequest(url, configurations)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader(loaderMessage))
                         .send(unmarshaller.newListUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Creates the file by given {@code path} with given {@code content}. Content may be an empty.
     *
     * @param path
     *         path to the future file
     * @param content
     *         the file content
     * @return {@link Promise} with the {@link ItemReference}
     * @see ItemReference
     * @see Path
     * @since 4.4.0
     */
    public Promise<ItemReference> createFile(Path path, String content) {
        final String url = encodeAllowEscapes(getBaseUrl() + FILE + path(path.parent().toString()) + "?name=" + path.lastSegment());

        return reqFactory.createPostRequest(url, null)
                         .data(content)
                         .loader(loaderFactory.newLoader("Creating file..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /**
     * Reads the file content by given {@code path}.
     *
     * @param path
     *         path to the file
     * @return {@link Promise} with file content
     * @see Path
     * @since 4.4.0
     */
    public Promise<String> getFileContent(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + FILE + path(path.toString()));

        return reqFactory.createGetRequest(url)
                         .loader(loaderFactory.newLoader("Loading file content..."))
                         .send(new StringUnmarshaller());
    }

    /**
     * Writes the file {@code content} by given {@code path}.
     *
     * @param path
     *         path to the file
     * @param content
     *         the file content
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    public Promise<Void> setFileContent(Path path, String content) {
        final String url = encodeAllowEscapes(getBaseUrl() + FILE + path(path.toString()));

        return reqFactory.createRequest(PUT, url, null, false)
                         .data(content)
                         .loader(loaderFactory.newLoader("Updating file..."))
                         .send();
    }

    /**
     * Creates the folder by given {@code path}.
     *
     * @param path
     *         path to the future folder
     * @return {@link Promise} with the {@link ItemReference}
     * @see ItemReference
     * @see Path
     * @since 4.4.0
     */
    public Promise<ItemReference> createFolder(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + FOLDER + path(path.toString()));

        return reqFactory.createPostRequest(url, null)
                         .loader(loaderFactory.newLoader("Creating folder..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /**
     * Removes the item by given {@code path} from the server.
     *
     * @param path
     *         path to the item
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    public Promise<Void> deleteItem(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + path(path.toString()));

        return reqFactory.createRequest(DELETE, url, null, false)
                         .loader(loaderFactory.newLoader("Deleting project..."))
                         .send();
    }

    /**
     * Copies the {@code source} item to given {@code target} with {@code newName}.
     *
     * @param source
     *         the source path to be copied
     * @param target
     *         the target path, should be a container (project or folder)
     * @param newName
     *         the new name of the copied item
     * @param overwrite
     *         overwrite target is such has already exists
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    public Promise<Void> copy(Path source, Path target, String newName, boolean overwrite) {
        final String url = encodeAllowEscapes(getBaseUrl() + COPY + path(source.toString()) + "?to=" + target.toString());

        final CopyOptions copyOptions = dtoFactory.createDto(CopyOptions.class);
        copyOptions.setName(newName);
        copyOptions.setOverWrite(overwrite);

        return reqFactory.createPostRequest(url, copyOptions)
                         .loader(loaderFactory.newLoader("Copying..."))
                         .send();
    }

    /**
     * Moves the {@code source} item to given {@code target} with {@code newName}.
     *
     * @param source
     *         the source path to be moved
     * @param target
     *         the target path, should be a container (project or folder)
     * @param newName
     *         the new name of the moved item
     * @param overwrite
     *         overwrite target is such has already exists
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    public Promise<Void> move(Path source, Path target, String newName, boolean overwrite) {
        final String url = encodeAllowEscapes(getBaseUrl() + MOVE + path(source.toString()) + "?to=" + target.toString());

        final MoveOptions moveOptions = dtoFactory.createDto(MoveOptions.class);
        moveOptions.setName(newName);
        moveOptions.setOverWrite(overwrite);

        return reqFactory.createPostRequest(url, moveOptions)
                         .loader(loaderFactory.newLoader("Moving..."))
                         .send();
    }

    /**
     * Reads the project tree starting from {@code path} with given {@code depth}.
     *
     * @param path
     *         the start point path where read should start
     * @param depth
     *         the depth to read, e.g. -1, 0 or less than {@link Integer#MAX_VALUE}
     * @param includeFiles
     *         include files into response
     * @return {@link Promise} with tree response
     * @see Path
     * @see TreeElement
     * @since 4.4.0
     */
    public Promise<TreeElement> getTree(Path path, int depth, boolean includeFiles) {
        final String url =
                encodeAllowEscapes(getBaseUrl() + TREE + path(path.toString()) + "?depth=" + depth + "&includeFiles=" + includeFiles);

        // temporary workaround for CHE-3467, remove loader for disable UI blocking
        // later this loader should be added with the new mechanism of client-server synchronization

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .send(unmarshaller.newUnmarshaller(TreeElement.class));
    }

    /**
     * Returns the item description by given {@code path}.
     *
     * @param path
     *         path to the item
     * @return {@link Promise} with the {@link ItemReference}
     * @see Path
     * @see ItemReference
     * @since 4.4.0
     */
    public Promise<ItemReference> getItem(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + ITEM + path(path.toString()));

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting item..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /**
     * Returns the specific project by given {@code path}. Path to project should be an absolute.
     *
     * @param path
     *         path to project
     * @return {@link Promise} with project configuration
     * @see ProjectConfigDto
     * @see Path
     * @since 4.4.0
     */
    public Promise<ProjectConfigDto> getProject(Path path) {
        final String url = encodeAllowEscapes(getBaseUrl() + path(path.toString()));

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Updates the project with the new {@code configuration} or creates the new one from existed folder on server side.
     *
     * @param configuration
     *         configuration which which be applied to the existed project
     * @return {@link Promise} with the applied {@link ProjectConfigDto}
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    public Promise<ProjectConfigDto> updateProject(ProjectConfigDto configuration) {
        final String url = encodeAllowEscapes(getBaseUrl() + path(configuration.getPath()));

        return reqFactory.createRequest(PUT, url, configuration, false)
                         .header(CONTENT_TYPE, APPLICATION_JSON)
                         .header(ACCEPT, APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Updating project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Returns the base url for the project service. It consists of workspace agent base url plus project prefix.
     *
     * @return base url for project service
     * @since 4.4.0
     */
    private String getBaseUrl() {
        return appContext.getWsAgentServerApiEndpoint() + PROJECT;
    }

    /**
     * Normalizes the path by adding a leading '/' if it doesn't exist.
     * Also escapes some special characters.
     * <p/>
     * See following javascript functions for details:
     * escape() will not encode: @ * / +
     * encodeURI() will not encode: ~ ! @ # $ & * ( ) = : / , ; ? + '
     * encodeURIComponent() will not encode: ~ ! * ( ) '
     *
     * @param path
     *         path to normalize
     * @return normalized path
     */
    private String path(String path) {
        while (path.indexOf('+') >= 0) {
            path = path.replace("+", "%2B");
        }

        return path.startsWith("/") ? path : '/' + path;
    }
}
