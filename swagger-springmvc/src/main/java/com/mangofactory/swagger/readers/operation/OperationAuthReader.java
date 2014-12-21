package com.mangofactory.swagger.readers.operation;

import com.mangofactory.swagger.authorization.AuthorizationContext;
import com.mangofactory.servicemodel.builder.AuthorizationBuilder;
import com.mangofactory.servicemodel.builder.AuthorizationScopeBuilder;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.annotations.AuthorizationScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class OperationAuthReader implements RequestMappingReader {

  private static final Logger LOG = LoggerFactory.getLogger(OperationAuthReader.class);
  @Override
  public void execute(RequestMappingContext context) {
    AuthorizationContext authorizationContext = (AuthorizationContext) context.get("authorizationContext");

    HandlerMethod handlerMethod = context.getHandlerMethod();
    String requestMappingPattern = (String) context.get("requestMappingPattern");
    List<com.mangofactory.servicemodel.Authorization> authorizations = newArrayList();

    if (null != authorizationContext) {
      authorizations = authorizationContext.getAuthorizationsForPath(requestMappingPattern);
    }

    ApiOperation apiOperationAnnotation = handlerMethod.getMethodAnnotation(ApiOperation.class);

    if (null != apiOperationAnnotation && null != apiOperationAnnotation.authorizations()) {
      Authorization[] authorizationAnnotations = apiOperationAnnotation.authorizations();
      if (authorizationAnnotations != null
              && authorizationAnnotations.length > 0
              && StringUtils.hasText(authorizationAnnotations[0].value())) {

        authorizations = newArrayList();
        for (Authorization authorization : authorizationAnnotations) {
          String value = authorization.value();
          AuthorizationScope[] scopes = authorization.scopes();
          List<com.mangofactory.servicemodel.AuthorizationScope> authorizationScopeList = newArrayList();
          for (AuthorizationScope authorizationScope : scopes) {
            String description = authorizationScope.description();
            String scope = authorizationScope.scope();
            authorizationScopeList.add(new AuthorizationScopeBuilder().scope(scope).description(description)
                    .build());
          }
          com.mangofactory.servicemodel.AuthorizationScope[] authorizationScopes = authorizationScopeList
                  .toArray(new com.mangofactory.servicemodel.AuthorizationScope[authorizationScopeList.size()]);
          com.mangofactory.servicemodel.Authorization authorizationModel =
                  new AuthorizationBuilder()
                          .type(value)
                          .scopes(authorizationScopes)
                          .build();
          authorizations.add(authorizationModel);
        }
      }
    }
    LOG.info("Authorization count {} for method {}", handlerMethod.getMethod().getName(), authorizations.size());
    context.put("authorizations", authorizations);
  }
}
