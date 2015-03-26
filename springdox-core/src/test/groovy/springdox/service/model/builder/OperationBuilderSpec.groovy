package springfox.service.model.builder

import spock.lang.Specification
import springfox.documentation.builders.OperationBuilder
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.Authorization
import springfox.documentation.service.Parameter
import springfox.documentation.service.ResponseMessage

import static com.google.common.collect.Sets.*

class OperationBuilderSpec extends Specification {
  OperationBuilder sut = new OperationBuilder()
  ResponseMessage partialOk = new ResponseMessageBuilder()
          .code(200)
          .message(null)
          .responseModel(null)
          .build()
  ResponseMessage fullOk = new ResponseMessageBuilder()
          .code(200)
          .message("OK")
          .responseModel(new ModelRef("String"))
          .build()

  def "Merges response messages when new response messages are applied" () {
    given:
      sut.responseMessages(newHashSet(partialOk))
    when:
      sut.responseMessages(newHashSet(fullOk))
    and:
      def operation = sut.build()
    then:
      operation.responseMessages.size() == 1
      operation.responseMessages.first().code == 200
      operation.responseMessages.first().message == "OK"
      operation.responseMessages.first().responseModel.type == "String"
      operation.responseMessages.first().responseModel.itemType == null
  }

  def "Response message builder is non-destructive" () {
    given:
      sut.responseMessages(newHashSet(fullOk))
    when:
      sut.responseMessages(newHashSet(partialOk))
    and:
      def operation = sut.build()
    then:
      operation.responseMessages.size() == 1
      operation.responseMessages.first().code == 200
      operation.responseMessages.first().message == "OK"
      operation.responseMessages.first().responseModel.type == "String"
      operation.responseMessages.first().responseModel.itemType == null
  }

  def "Setting properties on the builder with non-null values"() {
    given:
      def sut = new OperationBuilder()
    when:
      sut."$builderMethod"(value)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod     | value                   | property
      'method'          | 'method1'               | 'method'
      'summary'         | 'method1 summary'       | 'summary'
      'notes'           | 'method1 notes'         | 'notes'
      'responseModel'    | new ModelRef('string')  | 'responseModel'
      'deprecated'      | 'deprecated'            | 'deprecated'
      'nickname'        | 'method1'               | 'nickname'
      'produces'        | newHashSet('app/json')  | 'produces'
      'consumes'        | newHashSet('app/json')  | 'consumes'
      'protocols'       | newHashSet('https')     | 'protocol'
      'parameters'      | [Mock(Parameter)]       | 'parameters'
      'position'        | 1                       | 'position'
      'hidden'          | true                    | 'hidden'
  }

  def "Setting builder properties to null values preserves existing values"() {
    given:
      def sut = new OperationBuilder()
    when:
      sut."$builderMethod"(value)
      sut."$builderMethod"(null)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod     | value                   | property
      'method'          | 'method1'               | 'method'
      'summary'         | 'method1 summary'       | 'summary'
      'notes'           | 'method1 notes'         | 'notes'
      'responseModel'    | new ModelRef('string')  | 'responseModel'
      'deprecated'      | 'deprecated'            | 'deprecated'
      'nickname'        | 'method1'               | 'nickname'
      'produces'        | newHashSet('app/json')  | 'produces'
      'consumes'        | newHashSet('app/json')  | 'consumes'
      'protocols'       | newHashSet('https')     | 'protocol'
      'parameters'      | [Mock(Parameter)]       | 'parameters'
  }

  def "Operation authorizations are converted to a map by type"() {
    given:
      def sut = new OperationBuilder()
      def mockAuth1 = Mock(Authorization)
      def mockAuth2 = Mock(Authorization)
    and:
      mockAuth1.type >> "auth1"
      mockAuth1.scopes >> []
      mockAuth2.type >> "auth2"
      mockAuth2.scopes >> []
    and:
      def authorizations = [mockAuth1, mockAuth2]
    when:
      sut.authorizations(authorizations)
    and:
      def built = sut.build()
    then:
      built.authorizations.containsKey("auth1")
      built.authorizations.get("auth1") == mockAuth1.scopes
      built.authorizations.containsKey("auth2")
      built.authorizations.get("auth2") == mockAuth2.scopes

  }
}
