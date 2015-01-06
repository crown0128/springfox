package com.mangofactory.swagger.readers.operation.parameter
import com.mangofactory.springmvc.plugins.DocumentationContext
import com.mangofactory.swagger.mixins.DocumentationContextSupport
import com.mangofactory.swagger.mixins.RequestMappingSupport
import com.mangofactory.swagger.readers.Command
import com.mangofactory.swagger.scanners.RequestMappingContext
import com.wordnik.swagger.annotations.ApiParam
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import javax.servlet.ServletContext
import java.lang.annotation.Annotation

@Mixin([RequestMappingSupport, DocumentationContextSupport])
class ParameterRequiredReaderSpec extends Specification {
  DocumentationContext context = defaultContext(Mock(ServletContext))

  def "parameters required"() {
  given:
    HandlerMethod handlerMethod = Mock()
    RequestMappingContext context = new RequestMappingContext(context, requestMappingInfo("somePath"), handlerMethod)
    MethodParameter methodParameter = Mock(MethodParameter)
    methodParameter.getParameterAnnotations() >> (paramAnnotations as Annotation[])
    methodParameter.getParameterType() >> Object.class
    methodParameter.getMethodAnnotation(PathVariable.class) >> paramAnnotations.find { it instanceof PathVariable }
    context.put("methodParameter", methodParameter)

  when:
    Command operationCommand = new ParameterRequiredReader();
    operationCommand.execute(context)
  then:
    context.get('required') == expected
  where:
    paramAnnotations                                                                  | expected
    [[required: { -> false }] as ApiParam, [required: { -> false }] as PathVariable]  | true
    [[required: { -> false }] as ApiParam, [required: { -> false }] as RequestHeader] | false
    [[required: { -> true }] as RequestHeader]                                        | true
    [[required: { -> false }] as RequestHeader]                                       | false
    [[required: { -> true }] as ApiParam]                                             | true
    [[required: { -> false }] as ApiParam]                                            | false
    [[required: { -> true }] as RequestParam]                                         | true
    [[required: { -> false }] as RequestParam]                                        | false
    [[required: { -> true }] as ApiParam, [required: { -> false }] as RequestParam]   | true
    [[required: { -> false }] as ApiParam, [required: { -> true }] as RequestParam]   | true
    []                                                                                | false
    [null]                                                                            | false
  }

  def "should detect java.util.Optional parameters"() {
  given:
    HandlerMethod handlerMethod = Mock()
    RequestMappingContext context = new RequestMappingContext(context, requestMappingInfo("somePath"), handlerMethod)
    MethodParameter methodParameter = Mock(MethodParameter)
    methodParameter.getParameterAnnotations() >> (paramAnnotations as Annotation[])
    context.put("methodParameter", methodParameter)

    Class<?> fakeOptionalClass = new FakeOptional().class
    fakeOptionalClass.name = "java.util.Optional"
    methodParameter.getParameterType() >> fakeOptionalClass

  when:
    Command operationCommand = new ParameterRequiredReader();
    operationCommand.execute(context)
  then:
    context.get('required') == false
  where:
    paramAnnotations << [
      [[required: { -> true }] as RequestHeader],
      [[required: { -> false }] as RequestHeader],
      [[required: { -> true }] as RequestParam],
      [[required: { -> false }] as RequestParam],
    ]
  }
}

class FakeOptional {}
