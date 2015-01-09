package com.mangofactory.swagger.plugins.operation;

import com.mangofactory.schema.plugins.DocumentationType;
import com.mangofactory.spring.web.plugins.OperationBuilderPlugin;
import com.mangofactory.spring.web.plugins.OperationContext;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

@Component
public class OperationHttpMethodReader implements OperationBuilderPlugin {
  private static final Logger log = LoggerFactory.getLogger(OperationHttpMethodReader.class);

  @Override
  public void apply(OperationContext context) {
    HandlerMethod handlerMethod = context.getHandlerMethod();

    ApiOperation apiOperationAnnotation = handlerMethod.getMethodAnnotation(ApiOperation.class);

    if (apiOperationAnnotation != null && StringUtils.hasText(apiOperationAnnotation.httpMethod())) {
      String apiMethod = apiOperationAnnotation.httpMethod();
      try {
        RequestMethod.valueOf(apiMethod);
        context.operationBuilder().method(apiMethod);
      } catch (IllegalArgumentException e) {
        log.error("Invalid http method: " + apiMethod + "Valid ones are [" + RequestMethod.values() + "]", e);
      }
    }
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}
