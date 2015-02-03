package com.mangofactory.documentation.swagger.readers.operation;

import com.google.common.base.Optional;
import com.mangofactory.documentation.service.ResponseMessage;
import com.mangofactory.documentation.builders.ResponseMessageBuilder;
import com.mangofactory.documentation.spi.DocumentationType;
import com.mangofactory.documentation.spi.service.OperationBuilderPlugin;
import com.mangofactory.documentation.spi.service.contexts.OperationContext;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Set;

import static com.google.common.collect.Sets.*;
import static com.mangofactory.documentation.swagger.annotations.Annotations.*;
import static com.mangofactory.documentation.swagger.common.SwaggerPluginSupport.*;

@Component
public class SwaggerResponseMessageReader implements OperationBuilderPlugin {

  @Override
  public void apply(OperationContext context) {
    HandlerMethod handlerMethod = context.getHandlerMethod();
    context.operationBuilder()
            .responseMessages(read(handlerMethod));

  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }

  protected Set<ResponseMessage> read(HandlerMethod handlerMethod) {
    Optional<ApiResponses> apiResponsesOptional = findApiResponsesAnnotations(handlerMethod.getMethod());
    Set<ResponseMessage> responseMessages = newHashSet();
    if (apiResponsesOptional.isPresent()) {
      ApiResponse[] apiResponseAnnotations = apiResponsesOptional.get().value();
      for (ApiResponse apiResponse : apiResponseAnnotations) {
        String overrideTypeName = overrideTypeName(apiResponse);

        responseMessages.add(new ResponseMessageBuilder()
                .code(apiResponse.code())
                .message(apiResponse.message())
                .responseModel(overrideTypeName)
                .build());
      }
    }
    return responseMessages;
  }


  private String overrideTypeName(ApiResponse apiResponse) {
    if (apiResponse.response() != null) {
      return apiResponse.response().getSimpleName();
    }
    return "";
  }

}
