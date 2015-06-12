/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.spring.web.readers.parameter

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.classmate.TypeResolver
import io.swagger.annotations.ApiParam
import org.springframework.core.MethodParameter
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.service.ResolvedMethodParameter
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.GenericTypeNamingStrategy
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.ParameterContext
import springfox.documentation.spring.web.mixins.RequestMappingSupport
import springfox.documentation.spring.web.plugins.DocumentationContextSpec

import static org.springframework.http.MediaType.*

@Mixin([RequestMappingSupport])
class ParameterTypeReaderSpec extends DocumentationContextSpec {

  def "param type"() {
    given:
      MethodParameter methodParameter = Stub(MethodParameter)
      methodParameter.getParameterAnnotation(ApiParam.class) >> null
      methodParameter.getParameterAnnotations() >> [annotation]
      def resolvedMethodParameter = Mock(ResolvedMethodParameter)
      resolvedMethodParameter.methodParameter >> methodParameter
      resolvedMethodParameter.resolvedParameterType >> resolve(type)
      def operationContext = Mock(OperationContext)
    and:
      operationContext.consumes() >> consumes
      operationContext.httpMethod() >> httpMethod
      ParameterContext parameterContext = new ParameterContext(resolvedMethodParameter, new ParameterBuilder(),
          context(), Mock(GenericTypeNamingStrategy), operationContext)
    when:
      def operationCommand = new ParameterTypeReader()
      operationCommand.apply(parameterContext)
    then:
      parameterContext.parameterBuilder().build().paramType == expected
    where:
      annotation            | type          | consumes                              | httpMethod        | expected
      [:] as PathVariable   | Integer       | []                                    | HttpMethod.GET    |"path"
      [:] as ModelAttribute | Integer       | []                                    | HttpMethod.GET    |"body"
      [:] as RequestHeader  | Integer       | []                                    | HttpMethod.GET    |"header"
      [:] as RequestParam   | Integer       | []                                    | HttpMethod.GET    |"query"
      [:] as RequestParam   | Integer       | []                                    | HttpMethod.POST   |"query"
      [:] as RequestParam   | Integer       | [APPLICATION_JSON]                    | HttpMethod.GET    |"query"
      [:] as RequestParam   | Integer       | [APPLICATION_JSON]                    | HttpMethod.POST   |"query"
      [:] as RequestParam   | Integer       | [APPLICATION_FORM_URLENCODED]         | HttpMethod.POST   |"form"
      [:] as RequestPart    | Integer       | [MULTIPART_FORM_DATA]                 | HttpMethod.POST   |"form"
      null                  | Integer       | []                                    | HttpMethod.GET    |"body"
      null                  | MultipartFile | []                                    | HttpMethod.GET    |"form"
  }

  def "Should work with any documentationType"() {
    given:
      ParameterTypeReader sut = new ParameterTypeReader()
    expect:
      sut.supports(DocumentationType.SPRING_WEB)
      sut.supports(DocumentationType.SWAGGER_12)
      sut.supports(DocumentationType.SWAGGER_2)
  }

  ResolvedType resolve(Class clazz) {
    new TypeResolver().resolve(clazz);
  }
}
