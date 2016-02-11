/*
 *
 *  Copyright 2015 the original author or authors.
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

package springfox.documentation.spring.web.readers.operation;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationModelsProviderPlugin;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.lang.annotation.Annotation;
import java.util.List;

import static springfox.documentation.schema.ResolvedTypes.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperationModelsProvider implements OperationModelsProviderPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(OperationModelsProvider.class);
  private final TypeResolver typeResolver;

  @Autowired
  public OperationModelsProvider(TypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void apply(RequestMappingContext context) {
    collectFromReturnType(context);
    collectParameters(context);
    collectGlobalModels(context);
  }

  private void collectGlobalModels(RequestMappingContext context) {
    for (ResolvedType each : context.getDocumentationContext().getAdditionalModels()) {
      context.operationModelsBuilder().addInputParam(each);
      context.operationModelsBuilder().addReturn(each);
    }
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private void collectFromReturnType(RequestMappingContext context) {
    ResolvedType modelType = new HandlerMethodResolver(typeResolver).methodReturnType(context.getHandlerMethod());
    modelType = context.alternateFor(modelType);
    LOG.debug("Adding return parameter of type {}", resolvedTypeSignature(modelType).or("<null>"));
    context.operationModelsBuilder().addReturn(modelType);
  }

  private void collectParameters(RequestMappingContext context) {

    HandlerMethod handlerMethod = context.getHandlerMethod();

    LOG.debug("Reading parameters models for handlerMethod |{}|", handlerMethod.getMethod().getName());

    HandlerMethodResolver handlerMethodResolver = new HandlerMethodResolver(typeResolver);
    List<ResolvedMethodParameter> parameterTypes = handlerMethodResolver.methodParameters(handlerMethod);
    for (ResolvedMethodParameter parameterType : parameterTypes) {
      Annotation[] parameterAnnotations = parameterType.getMethodParameter().getParameterAnnotations();
      for (Annotation annotation : parameterAnnotations) {
        if (annotation instanceof RequestBody || annotation instanceof RequestPart) {
          ResolvedType modelType = context.alternateFor(parameterType.getResolvedParameterType());
          LOG.debug("Adding input parameter of type {}", resolvedTypeSignature(modelType).or("<null>"));
          context.operationModelsBuilder().addInputParam(modelType);
        }
      }
    }
    LOG.debug("Finished reading parameters models for handlerMethod |{}|", handlerMethod.getMethod().getName());
  }
}
