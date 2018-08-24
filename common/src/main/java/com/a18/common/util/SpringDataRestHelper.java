package com.a18.common.util;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.projection.ProjectionDefinitions;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.rest.webmvc.support.PersistentEntityProjector;
import org.springframework.data.rest.webmvc.support.Projector;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;

// The magic is copied from here
// https://gist.github.com/iznenad/424d49696f78cd7563e11ff5d51f21f4
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SpringDataRestHelper {

  private final Associations associations;

  private final SelfLinkProvider linkProvider;

  private final PersistentEntities entities;

  private final RepositoryRestConfiguration repositoryRestConfiguration;

  private PersistentEntityResourceAssembler persistentEntityResourceAssembler;

  private final NativeWebRequest nativeWebRequest;

  @Autowired public SpringDataRestHelper(
      Associations associations,
      SelfLinkProvider linkProvider,
      PersistentEntities entities,
      RepositoryRestConfiguration repositoryRestConfiguration,
      NativeWebRequest nativeWebRequest
  ) {
    this.associations = associations;
    this.linkProvider = linkProvider;
    this.entities = entities;
    this.repositoryRestConfiguration = repositoryRestConfiguration;
    this.nativeWebRequest = nativeWebRequest;
  }

  @PostConstruct void init() {
    persistentEntityResourceAssembler = initResourceAssembler();
  }

  private PersistentEntityResourceAssembler initResourceAssembler() {
    ProjectionDefinitions projectionDefinitions =
        repositoryRestConfiguration.getProjectionConfiguration();
    ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
    String projection = nativeWebRequest.getParameter(projectionDefinitions.getParameterName());
    Projector projector =
        new PersistentEntityProjector(projectionDefinitions, factory, projection,
            associations.getMappings()
        );
    return new PersistentEntityResourceAssembler(entities, projector, associations, linkProvider);
  }

  public <T> ResourceAssembler<T, PersistentEntityResource> resourceAssembler() {
    return entity -> persistentEntityResourceAssembler.toResource(entity);
  }

  public <T> PersistentEntityResource toResource(T entity) {
    return persistentEntityResourceAssembler.toResource(entity);
  }
}