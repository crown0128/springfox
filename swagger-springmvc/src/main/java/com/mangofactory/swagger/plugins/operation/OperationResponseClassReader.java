package com.mangofactory.swagger.plugins.operation;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.mangofactory.schema.TypeNameExtractor;
import com.mangofactory.schema.alternates.AlternateTypeProvider;
import com.mangofactory.schema.plugins.DocumentationType;
import com.mangofactory.spring.web.plugins.OperationBuilderPlugin;
import com.mangofactory.spring.web.plugins.OperationContext;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import static com.mangofactory.schema.plugins.ModelContext.*;
import static com.mangofactory.spring.web.HandlerMethodReturnTypes.*;

@Component("swaggerOperationClassReader")
public class OperationResponseClassReader implements OperationBuilderPlugin {
  private static Logger log = LoggerFactory.getLogger(OperationResponseClassReader.class);
  private final TypeResolver typeResolver;
  private final AlternateTypeProvider alternateTypeProvider;
  private final TypeNameExtractor nameExtractor;

  @Autowired
  public OperationResponseClassReader(TypeResolver typeResolver, AlternateTypeProvider alternateTypeProvider,
                                      TypeNameExtractor nameExtractor) {
    this.typeResolver = typeResolver;
    this.alternateTypeProvider = alternateTypeProvider;
    this.nameExtractor = nameExtractor;
  }

  @Override
  public void apply(OperationContext context) {

    HandlerMethod handlerMethod = context.getHandlerMethod();
    ApiOperation methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ApiOperation.class);
    ResolvedType returnType;
    if (null != methodAnnotation && Void.class != methodAnnotation.response()) {
      log.debug("Overriding response class with annotated response class");
      returnType = typeResolver.resolve(methodAnnotation.response());
    } else {
      returnType = handlerReturnType(typeResolver, handlerMethod);
      returnType = alternateTypeProvider.alternateFor(returnType);
    }
    if (Void.class.equals(returnType.getErasedType()) || Void.TYPE.equals(returnType.getErasedType())) {
      context.operationBuilder().responseClass("void");
      return;
    }
    String responseTypeName = nameExtractor.typeName(returnValue(returnType, context.getDocumentationType()));
    log.debug("Setting response class to:" + responseTypeName);
    context.operationBuilder().responseClass(responseTypeName);
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}
