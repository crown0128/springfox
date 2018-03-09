/*
 *
 *  Copyright 2017-2018 the original author or authors.
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

import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.rest.webmvc.mapping.Associations;
import springfox.documentation.RequestHandler;

import java.util.List;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class EntityAssociationsExtractor implements EntityOperationsExtractor {

  @Override
  public List<RequestHandler> extract(final EntityContext context) {

    final List<RequestHandler> handlers = new ArrayList<>();
    final Associations associations = context.getAssociations();

    context.entity()
      .ifPresent(entity -> entity.doWithAssociations((SimpleAssociationHandler) association -> {

        if (associations.isLinkableAssociation(association)) {
          final EntityAssociationContext associationContext = new EntityAssociationContext(context, association);

          handlers.addAll(context.getAssociationExtractors().stream()
            .flatMap(extractor -> extractor.extract(associationContext).stream())
            .collect(Collectors.toList()));
        }
      }));

    return handlers;
  }

}
