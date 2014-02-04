package com.mangofactory.swagger.readers.operation;

import com.mangofactory.swagger.readers.Command;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import static com.mangofactory.swagger.core.ModelUtils.getHandlerReturnType;

public class OperationResponseClassReader implements Command<RequestMappingContext> {
   private static Logger log = LoggerFactory.getLogger(OperationResponseClassReader.class);

   @Override
   public void execute(RequestMappingContext context) {
      HandlerMethod handlerMethod = context.getHandlerMethod();
      ApiOperation methodAnnotation = handlerMethod.getMethodAnnotation(ApiOperation.class);
      Class<?> returnType = Void.class;
      if ((null != methodAnnotation) && null != methodAnnotation.response()) {
         log.debug("Overriding response class with annotated response class");
         returnType = methodAnnotation.response();
      } else {
         returnType = getHandlerReturnType(handlerMethod);
      }
      String canonicalName = returnType.getCanonicalName();
      log.debug("Setting response class to:" + canonicalName);
      context.put("responseClass", canonicalName);
   }
}
