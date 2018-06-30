/*
 *
 *  Copyright 2017-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.documentation.spring.data.rest;

import com.fasterxml.classmate.TypeResolver;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.stereotype.Component;
import springfox.documentation.RequestHandler;
import springfox.documentation.spi.service.RequestHandlerProvider;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;


import static java.util.stream.Collectors.toList;

@Component
class EntityServicesProvider implements RequestHandlerProvider {
  private final RepositoryRestConfiguration configuration;
  private final ResourceMappings mappings;
  private final Repositories repositories;
  private final TypeResolver typeResolver;
  private final PersistentEntities entities;
  private final Associations associations;

  @Autowired(required = false)
  private RequestHandlerExtractorConfiguration extractorConfiguration;

  @Autowired
  public EntityServicesProvider(
      RepositoryRestConfiguration configuration,
      ResourceMappings mappings,
      Repositories repositories,
      TypeResolver typeResolver,
      PersistentEntities entities,
      Associations associations) {
    this.mappings = mappings;
    this.configuration = configuration;
    this.repositories = repositories;
    this.typeResolver = typeResolver;
    this.entities = entities;
    this.associations = associations;
  }

  @PostConstruct
  public void init() {
    if (extractorConfiguration == null) {
      extractorConfiguration = new DefaultExtractorConfiguration();
    }
  }

  @Override
  public List<RequestHandler> requestHandlers() {
    List<EntityContext> contexts = new ArrayList<>();
    for (Class each : repositories) {
      Object repositoryInformation = repositories.getRepositoryInformationFor(each);
      Object repositoryInstance = repositories.getRepositoryFor(each);
      ResourceMetadata resource = mappings.getMetadataFor(each);
      if (resource.isExported()) {
        OptionalDeferencer<RepositoryInformation> converter = new OptionalDeferencer<>();
        RepositoryInformation repositoryInfo =
            converter.convert(repositoryInformation);
        contexts.add(new EntityContext(
            typeResolver,
            configuration,
            repositoryInfo,
            repositoryInstance,
            resource,
            mappings,
            entities,
            associations, extractorConfiguration));
      }

    }

    List<RequestHandler> handlers = new ArrayList<RequestHandler>();
    for (EntityContext each : contexts) {
      handlers.addAll(extractorConfiguration.getEntityExtractors().stream()
          .map(extractFromContext(each))
          .flatMap(Collection::stream)
          .collect(toList()));
    }
    return handlers;
  }

  private Function<EntityOperationsExtractor, List<RequestHandler>> extractFromContext(final EntityContext context) {
    return input -> input.extract(context);
  }

}
