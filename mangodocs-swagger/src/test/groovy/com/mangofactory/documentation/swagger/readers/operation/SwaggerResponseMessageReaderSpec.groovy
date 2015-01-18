package com.mangofactory.documentation.swagger.readers.operation
import com.fasterxml.classmate.TypeResolver
import com.mangofactory.documentation.service.model.builder.OperationBuilder
import com.mangofactory.documentation.spi.service.contexts.OperationContext
import com.mangofactory.documentation.spring.web.DocumentationContextSpec
import com.mangofactory.documentation.spring.web.mixins.ServicePluginsSupport
import com.mangofactory.documentation.spring.web.mixins.RequestMappingSupport
import org.springframework.web.bind.annotation.RequestMethod

@Mixin([RequestMappingSupport, ServicePluginsSupport])
class SwaggerResponseMessageReaderSpec extends DocumentationContextSpec {

  def "swagger annotation should override when using swagger reader"() {
    given:
      OperationContext operationContext = new OperationContext(new OperationBuilder(),
              RequestMethod.GET, dummyHandlerMethod('methodWithApiResponses'), 0, requestMappingInfo('/somePath'),
              context(), "")
    when:
      new SwaggerResponseMessageReader(new TypeResolver()).apply(operationContext)
    and:
      def operation = operationContext.operationBuilder().build()
      def responseMessages = operation.responseMessages

    then:
      responseMessages.size() == 1
      def annotatedResponse = responseMessages.find { it.code == 413 }
      annotatedResponse != null
      annotatedResponse.message == "a message"
  }
}
