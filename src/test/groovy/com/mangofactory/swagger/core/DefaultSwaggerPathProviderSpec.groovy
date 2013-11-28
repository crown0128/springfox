package com.mangofactory.swagger.core

import com.mangofactory.swagger.mixins.RequestMappingSupport
import spock.lang.Specification
import spock.lang.Unroll

@Mixin(RequestMappingSupport)
class DefaultSwaggerPathProviderSpec extends Specification {

   @Unroll
   def "Swagger url formats"() {
    given:
      DefaultSwaggerPathProvider defaultSwaggerPathProvider = new DefaultSwaggerPathProvider(apiResourceSuffix: "/api/v1/");
      defaultSwaggerPathProvider.servletContext = servletContext()

    expect:
      defaultSwaggerPathProvider."${method}"() == expected
    where:
      method                            | expected
      "getAppBasePath"                  | "http://127.0.0.1:8080/context-path"
      "getSwaggerDocumentationBasePath" | 'http://127.0.0.1:8080/context-path/api-docs/'
      "getApiResourcePrefix"            | "/api/v1/"
   }
}
