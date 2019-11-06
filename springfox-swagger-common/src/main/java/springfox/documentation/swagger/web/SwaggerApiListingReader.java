/*
 *
 *  Copyright 2015-2019 the original author or authors.
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
package springfox.documentation.swagger.web;


import io.swagger.annotations.Api;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.*;
import static java.util.stream.Collectors.*;
import static org.springframework.core.annotation.AnnotationUtils.*;
import static springfox.documentation.service.Tags.*;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.*;

@Component
@Order(value = SWAGGER_PLUGIN_ORDER)
public class SwaggerApiListingReader implements ApiListingBuilderPlugin {
  @Override
  public void apply(ApiListingContext apiListingContext) {
    Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup().getControllerClass();
    if (controller.isPresent()) {
      Optional<Api> apiAnnotation = ofNullable(findAnnotation(controller.get(), Api.class));
      String description =
          apiAnnotation.map(Api::description).filter(((Predicate<String>) String::isEmpty).negate())
              .orElse(null);

      Set<String> tagSet = apiAnnotation.map(tags())
          .orElse(new TreeSet<>());
      if (tagSet.isEmpty()) {
        tagSet.add(apiListingContext.getResourceGroup().getGroupName());
      }
      apiListingContext.apiListingBuilder()
          .description(description)
          .tagNames(tagSet);
    }
  }

  private Function<Api, Set<String>> tags() {
    return input -> Stream.of(input.tags())
        .filter(emptyTags())
        .collect(toCollection(TreeSet::new));
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
