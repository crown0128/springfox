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
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.Maps;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.HandlerMethodReturnTypes;

import java.util.List;

import static com.google.common.base.Optional.*;
import static com.google.common.collect.Sets.*;
import static org.springframework.core.annotation.AnnotationUtils.*;
import static springfox.documentation.schema.Types.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseMessagesReader implements OperationBuilderPlugin {

  private final TypeResolver typeResolver;
  private final TypeNameExtractor typeNameExtractor;

  @Autowired
  public ResponseMessagesReader(TypeResolver typeResolver,
                                TypeNameExtractor typeNameExtractor) {
    this.typeResolver = typeResolver;
    this.typeNameExtractor = typeNameExtractor;
  }

  @Override
  public void apply(OperationContext context) {
    List<ResponseMessage> responseMessages = context.getGlobalResponseMessages(context.httpMethod().toString());
    context.operationBuilder().responseMessages(newHashSet(responseMessages));
    applyReturnTypeOverride(context);
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private void applyReturnTypeOverride(OperationContext context) {

    ResolvedType returnType = HandlerMethodReturnTypes.handlerReturnType(typeResolver, context.getHandlerMethod());
    returnType = context.alternateFor(returnType);
    int httpStatusCode = httpStatusCode(context.getHandlerMethod());
    String message = message(context.getHandlerMethod());
    ModelRef modelRef = null;
    if (!isVoid(returnType)) {
      ModelContext modelContext = ModelContext.returnValue(returnType,
              context.getDocumentationType(), context.getAlternateTypeProvider(),
              context.getDocumentationContext().getGenericsNamingStrategy());
      modelRef = modelRef(returnType, modelContext);
    }
    ResponseMessage built = new ResponseMessageBuilder()
            .code(httpStatusCode)
            .message(message)
            .responseModel(modelRef)
            .build();
    context.operationBuilder().responseMessages(newHashSet(built));
  }


  private ModelRef modelRef(ResolvedType type, ModelContext modelContext) {
    if (Collections.isContainerType(type)) {
      ResolvedType collectionElementType = Collections.collectionElementType(type);
      String elementTypeName = typeNameExtractor.typeName(ModelContext.fromParent(modelContext, collectionElementType));
      return new ModelRef(Collections.containerType(type), elementTypeName);
    }
    if (Maps.isMapType(type)) {
      String elementTypeName = typeNameExtractor.typeName(ModelContext.fromParent(modelContext, Maps.mapValueType
              (type)));
      return new ModelRef("Map", elementTypeName, true);
    }
    String typeName = typeNameExtractor.typeName(ModelContext.fromParent(modelContext, type));
    return new ModelRef(typeName);
  }


  public static int httpStatusCode(HandlerMethod handlerMethod) {
    Optional<ResponseStatus> responseStatus
            = fromNullable(getAnnotation(handlerMethod.getMethod(), ResponseStatus.class));
    int httpStatusCode = HttpStatus.OK.value();
    if (responseStatus.isPresent()) {
      httpStatusCode = responseStatus.get().value().value();
    }
    return httpStatusCode;
  }

  public static String message(HandlerMethod handlerMethod) {
    Optional<ResponseStatus> responseStatus
            = fromNullable(getAnnotation(handlerMethod.getMethod(), ResponseStatus.class));
    String reasonPhrase = HttpStatus.OK.getReasonPhrase();
    if (responseStatus.isPresent()) {
      reasonPhrase = responseStatus.get().reason();
    }
    return reasonPhrase;
  }

}
