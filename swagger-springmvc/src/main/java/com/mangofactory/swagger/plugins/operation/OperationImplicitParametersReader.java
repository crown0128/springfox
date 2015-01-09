package com.mangofactory.swagger.plugins.operation;

import com.google.common.collect.Lists;
import com.mangofactory.schema.plugins.DocumentationType;
import com.mangofactory.service.model.Parameter;
import com.mangofactory.spring.web.plugins.OperationBuilderPlugin;
import com.mangofactory.spring.web.plugins.OperationContext;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

@Component
public class OperationImplicitParametersReader implements OperationBuilderPlugin {

  @Override
  public void apply(OperationContext context) {
    context.operationBuilder().parameters(readParameters(context));
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
  protected List<Parameter> readParameters(OperationContext context) {
    HandlerMethod handlerMethod = context.getHandlerMethod();
    Method method = handlerMethod.getMethod();
    ApiImplicitParams annotation = AnnotationUtils.findAnnotation(method, ApiImplicitParams.class);

    List<Parameter> parameters = Lists.newArrayList();
    if (null != annotation) {
      for (ApiImplicitParam param : annotation.value()) {
        parameters.add(OperationImplicitParameterReader.getImplicitParameter(param));
      }
    }

    return parameters;
  }
}
