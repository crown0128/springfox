package com.mangofactory.swagger.readers;

import com.mangofactory.service.model.ApiDescription;
import com.mangofactory.service.model.Operation;
import com.mangofactory.service.model.builder.ApiDescriptionBuilder;
import com.mangofactory.swagger.core.RequestMappingEvaluator;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.List;

import static com.google.common.collect.Lists.*;

@Component
public class ApiDescriptionReader implements Command<RequestMappingContext> {

  private final ApiOperationReader operationReader;

  @Autowired
  public ApiDescriptionReader(ApiOperationReader operationReader) {
    this.operationReader = operationReader;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute(RequestMappingContext context) {
    RequestMappingInfo requestMappingInfo = context.getRequestMappingInfo();
    HandlerMethod handlerMethod = context.getHandlerMethod();
    PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
    RequestMappingEvaluator requestMappingEvaluator = context.getDocumentationContext().getRequestMappingEvaluator();
    SwaggerPathProvider swaggerPathProvider = context.getDocumentationContext().getSwaggerPathProvider();

    List<ApiDescription> apiDescriptionList = newArrayList();
    for (String pattern : patternsCondition.getPatterns()) {
      if (requestMappingEvaluator.shouldIncludePath(pattern)) {
        String cleanedRequestMappingPath = sanitizeRequestMappingPattern(pattern);
        String path = swaggerPathProvider.getOperationPath(cleanedRequestMappingPath);
        String methodName = handlerMethod.getMethod().getName();
        context.put("requestMappingPattern", cleanedRequestMappingPath);
        operationReader.execute(context);
        List<Operation> operations = (List<Operation>) context.get("operations");
        apiDescriptionList.add(
                new ApiDescriptionBuilder()
                        .path(path)
                        .description(methodName)
                        .operations(operations)
                        .hidden(false)
                        .build());
      }
    }
    context.put("apiDescriptionList", apiDescriptionList);
  }

  /**
   * Gets a uri friendly path from a request mapping pattern.
   * Typically involves removing any regex patterns or || conditions from a spring request mapping
   * This method will be called to resolve every request mapping endpoint.
   * A good extension point if you need to alter endpoints by adding or removing path segments.
   * Note: this should not be an absolute  uri
   *
   * @param requestMappingPattern
   * @return the request mapping endpoint
   */
  static String sanitizeRequestMappingPattern(String requestMappingPattern) {
    String result = requestMappingPattern;
    //remove regex portion '/{businessId:\\w+}'
    result = result.replaceAll("\\{([^}]*?):.*?\\}", "{$1}");
    return result.isEmpty() ? "/" : result;
  }
}
