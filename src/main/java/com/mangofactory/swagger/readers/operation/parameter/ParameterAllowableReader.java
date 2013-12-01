package com.mangofactory.swagger.readers.operation.parameter;

import com.google.common.base.Splitter;
import com.mangofactory.swagger.readers.Command;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableValues;
import org.springframework.core.MethodParameter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mangofactory.swagger.ScalaUtils.toScalaList;

public class ParameterAllowableReader implements Command<RequestMappingContext> {


   @Override
   public void execute(RequestMappingContext context) {
      MethodParameter methodParameter = (MethodParameter) context.get("methodParameter");
      AllowableValues allowableValues = null;
      String allowableValueString = findAnnotatedAllowableValues(methodParameter);
      if (null != allowableValueString && allowableValueString.contains(",")) {
         Iterable<String> split = Splitter.on(',').trimResults().omitEmptyStrings().split(allowableValueString);
         allowableValues = new AllowableListValues(toScalaList(newArrayList(split)), "LIST");
      } else {
         if (methodParameter.getParameterType().isEnum()) {
            Object[] enumConstants = methodParameter.getParameterType().getEnumConstants();
            List<String> enumNames = new ArrayList<String>();
            for (Object o : enumConstants) {
               enumNames.add(o.toString());
            }
            allowableValues = new AllowableListValues(toScalaList(newArrayList(enumNames)), "LIST");
         }
      }
      context.put("allowableValues", allowableValues);

      context.put("dataType", "integer");
      context.put("format", "int64");
      context.put("paramAccess", "");
   }

   private String findAnnotatedAllowableValues(MethodParameter methodParameter) {
      Annotation[] methodAnnotations = methodParameter.getParameterAnnotations();
      if (null != methodAnnotations) {
         for (Annotation annotation : methodAnnotations) {
            if (annotation instanceof ApiParam) {
               return ((ApiParam) annotation).allowableValues();
            }
         }
      }
      return null;
   }
}
