package com.mangofactory.swagger.readers

import com.mangofactory.swagger.configuration.SpringSwaggerConfig
import com.mangofactory.swagger.configuration.SwaggerGlobalSettings
import com.mangofactory.swagger.core.AbsoluteSwaggerPathProvider
import com.mangofactory.swagger.mixins.RequestMappingSupport
import com.mangofactory.swagger.mixins.SwaggerPathProviderSupport
import com.mangofactory.swagger.scanners.RequestMappingContext
import com.wordnik.swagger.model.ApiDescription
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import spock.lang.Specification

import static com.mangofactory.swagger.ScalaUtils.fromOption

@Mixin([RequestMappingSupport, SwaggerPathProviderSupport])
class ApiDescriptionReaderSpec extends Specification {

   def "should generate an api description for each request mapping pattern"() {
    given:

      ApiDescriptionReader apiDescriptionReader = new ApiDescriptionReader(absoluteSwaggerPathProvider())
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/doesNotMatterForThisTest",
              [patternsRequestCondition: patternsRequestCondition('/somePath/{businessId}', '/somePath/{businessId:\\d+}')]
      )

      HandlerMethod handlerMethod = dummyHandlerMethod()
      RequestMappingContext context = new RequestMappingContext(requestMappingInfo, handlerMethod)

      def settings = new SwaggerGlobalSettings()
      SpringSwaggerConfig springSwaggerConfig = new SpringSwaggerConfig()
      settings.alternateTypeProvider = springSwaggerConfig.defaultAlternateTypeProvider();
      context.put("swaggerGlobalSettings", settings)
    when:
      apiDescriptionReader.execute(context)
      Map<String, Object> result = context.getResult()

    then:
      def descriptionList = result['apiDescriptionList']
      descriptionList.size == 2

      ApiDescription apiDescription = descriptionList[0]
      ApiDescription secondApiDescription = descriptionList[1]

      apiDescription.path() == '/api/v1/somePath/{businessId}'
      fromOption(apiDescription.description()) == dummyHandlerMethod().method.name

      secondApiDescription.path() == '/api/v1/somePath/{businessId}'
      fromOption(secondApiDescription.description()) == dummyHandlerMethod().method.name
   }

   def "should sanitize request mapping endpoints"() {
      expect:
        new ApiDescriptionReader(absoluteSwaggerPathProvider()).sanitizeRequestMappingPattern(mappingPattern) == expected

      where:
        mappingPattern             | expected
        ""                         | "/"
        "/"                        | "/"
        "/businesses"              | "/businesses"
        "/{businessId:\\w+}"       | "/{businessId}"
        "/businesses/{businessId}" | "/businesses/{businessId}"
        "/foo/bar:{baz}"           | "/foo/bar:{baz}"
        "/foo/bar:{baz:\\w+}"      | "/foo/bar:{baz}"

   }
}
