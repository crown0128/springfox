package com.mangofactory.swagger.readers

import com.fasterxml.classmate.TypeResolver
import com.mangofactory.swagger.authorization.AuthorizationContext
import com.mangofactory.swagger.configuration.SwaggerGlobalSettings
import com.mangofactory.swagger.mixins.AuthSupport
import com.mangofactory.swagger.mixins.RequestMappingSupport
import com.mangofactory.swagger.models.configuration.SwaggerModelsConfiguration
import com.mangofactory.swagger.scanners.RegexRequestMappingPatternMatcher
import com.mangofactory.swagger.scanners.RequestMappingContext
import com.mangofactory.swagger.models.dto.Operation
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import spock.lang.Specification

import static org.springframework.web.bind.annotation.RequestMethod.*

@Mixin([RequestMappingSupport, AuthSupport])
class ApiOperationReaderSpec extends Specification {

  def "Should generate default operation on handler method without swagger annotations"() {

    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/doesNotMatterForThisTest",
              [
                      patternsRequestCondition      : patternsRequestCondition('/doesNotMatterForThisTest', '/somePath/{businessId:\\d+}'),
                      requestMethodsRequestCondition: requestMethodsRequestCondition(PATCH, POST)
              ]
      )

      HandlerMethod handlerMethod = dummyHandlerMethod()
      RequestMappingContext context = new RequestMappingContext(requestMappingInfo, handlerMethod)
      AuthorizationContext authorizationContext = new AuthorizationContext.AuthorizationContextBuilder(defaultAuth())
              .withRequestMappingPatternMatcher(new RegexRequestMappingPatternMatcher())
              .withIncludePatterns([".*"])
              .withRequestMethods(values())
              .build()

      def settings = new SwaggerGlobalSettings()
      SwaggerModelsConfiguration springSwaggerConfig = new SwaggerModelsConfiguration()
      settings.alternateTypeProvider = springSwaggerConfig.alternateTypeProvider(new TypeResolver());
      context.put("swaggerGlobalSettings", settings)
      context.put("requestMappingPattern", "/anything")
      context.put("authorizationContext", authorizationContext)

      ApiOperationReader apiOperationReader = new ApiOperationReader()

    when:
      apiOperationReader.execute(context)
      Map<String, Object> result = context.getResult()

    then:
      Operation apiOperation = result['operations'][0]
      apiOperation.getMethod() == PATCH.toString()
      apiOperation.getSummary() == handlerMethod.method.name
      apiOperation.getNotes() == handlerMethod.method.name
      apiOperation.getNickname() == handlerMethod.method.name
      apiOperation.getPosition() == 0
      apiOperation.getAuthorizations().size() == 1

      def secondApiOperation = result['operations'][1]
      secondApiOperation.position == 1
  }


  def "Should ignore operations that are marked as hidden"() {

    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/doesNotMatterForThisTest",
              [
                      patternsRequestCondition      : patternsRequestCondition('/doesNotMatterForThisTest', '/somePath/{businessId:\\d+}'),
                      requestMethodsRequestCondition: requestMethodsRequestCondition(PATCH, POST)
              ]
      )

      HandlerMethod handlerMethod = dummyHandlerMethod("methodThatIsHidden")
      RequestMappingContext context = new RequestMappingContext(requestMappingInfo, handlerMethod)
      AuthorizationContext authorizationContext = new AuthorizationContext.AuthorizationContextBuilder(defaultAuth())
              .withRequestMappingPatternMatcher(new RegexRequestMappingPatternMatcher())
              .withIncludePatterns([".*"])
              .withRequestMethods(values())
              .build()

      def settings = new SwaggerGlobalSettings()
      SwaggerModelsConfiguration springSwaggerConfig = new SwaggerModelsConfiguration()
      settings.alternateTypeProvider = springSwaggerConfig.alternateTypeProvider(new TypeResolver());
      context.put("swaggerGlobalSettings", settings)
      context.put("requestMappingPattern", "/anything")
      context.put("authorizationContext", authorizationContext)

      ApiOperationReader apiOperationReader = new ApiOperationReader()

    when:
      apiOperationReader.execute(context)
      Map<String, Object> result = context.getResult()

    then:
      0 == result['operations'].size()
  }
}
