package com.mangofactory.swagger.readers.operation;

import com.mangofactory.swagger.readers.operation.RequestMappingReader;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;

public class OperationNotesReader implements RequestMappingReader {
   @Override
   public void execute(RequestMappingContext context) {

      HandlerMethod handlerMethod = context.getHandlerMethod();
      String notes = handlerMethod.getMethod().getName();
      ApiOperation methodAnnotation = handlerMethod.getMethodAnnotation(ApiOperation.class);
      if((null != methodAnnotation) && !StringUtils.isBlank(methodAnnotation.notes())){
         notes = methodAnnotation.notes();
      }
      context.put("notes", notes);
   }
}
