package com.mangofactory.swagger.plugins.operation;

import com.mangofactory.schema.plugins.DocumentationType;
import com.mangofactory.springmvc.plugins.OperationBuilderPlugin;
import com.mangofactory.springmvc.plugins.OperationContext;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class OperationHiddenReader implements OperationBuilderPlugin {

  @Override
  public void apply(OperationContext context) {

    HandlerMethod handlerMethod = context.getHandlerMethod();
    ApiOperation methodAnnotation = handlerMethod.getMethodAnnotation(ApiOperation.class);
    if (null != methodAnnotation) {
      context.operationBuilder().hidden(methodAnnotation.hidden());
    }
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}
