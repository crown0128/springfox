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

package springfox.documentation.swagger.readers.operation;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.springframework.util.StringUtils.*;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class OperationAuthReader implements OperationBuilderPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(OperationAuthReader.class);

  @Override
  public void apply(OperationContext context) {

    List<SecurityContext> securityContexts = context.securityContext();

    Map<String, SecurityReference> securityReferences = new HashMap<>();

    for (SecurityContext each : securityContexts) {
      securityReferences.putAll(
          each.securityForOperation(context).stream()
              .collect(toMap(byReferenceName(), identity())));
    }
    maybeProcessApiOperation(context, securityReferences);
    LOG.debug("Authorization count {} for method {}", securityReferences.size(), context.getName());
    context.operationBuilder().authorizations(securityReferences.values());
  }

  private void maybeProcessApiOperation(
      OperationContext context,
      Map<String, SecurityReference> securityReferences) {
    Optional<ApiOperation> apiOperationAnnotation = context.findAnnotation(ApiOperation.class);

    apiOperationAnnotation.ifPresent(apiOp -> {
      List<SecurityReference> securityReferenceOverrides = new ArrayList<>();
      for (Authorization authorization : authorizationReferences(apiOperationAnnotation.get())) {
        String value = authorization.value();
        AuthorizationScope[] scopes = authorization.scopes();
        List<springfox.documentation.service.AuthorizationScope> authorizationScopeList = new ArrayList<>();
        for (AuthorizationScope authorizationScope : scopes) {
          String description = authorizationScope.description();
          String scope = authorizationScope.scope();
          // @Authorization has a default blank authorization scope, which we need to
          // ignore in the case of api keys.
          if (!isEmpty(scope)) {
            authorizationScopeList.add(
                new AuthorizationScopeBuilder()
                    .scope(scope)
                    .description(description)
                    .build());
          }
        }
        springfox.documentation.service.AuthorizationScope[] authorizationScopes
            = authorizationScopeList
            .toArray(new springfox.documentation.service.AuthorizationScope[0]);
        SecurityReference securityReference =
            SecurityReference.builder()
                             .reference(value)
                             .scopes(authorizationScopes)
                             .build();
        securityReferenceOverrides.add(securityReference);
      }
      securityReferences.putAll(securityReferenceOverrides.stream()
                                                          .collect(toMap(byReferenceName(), identity())));
    });
  }

  private Function<SecurityReference, String> byReferenceName() {
    return SecurityReference::getReference;
  }

  private Iterable<Authorization> authorizationReferences(ApiOperation apiOperationAnnotation) {
    return Stream.of(apiOperationAnnotation.authorizations())
                 .filter(input -> !isEmpty(input.value())).collect(toList());
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }
}
