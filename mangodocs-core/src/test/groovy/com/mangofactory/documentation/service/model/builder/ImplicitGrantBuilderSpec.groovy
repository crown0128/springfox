package com.mangofactory.documentation.service.model.builder

import com.mangofactory.documentation.service.model.LoginEndpoint
import spock.lang.Specification

class ImplicitGrantBuilderSpec extends Specification {
  def "Setting properties on the builder with non-null values"() {
    given:
      def sut = new ImplicitGrantBuilder()
    when:
      sut."$builderMethod"(value)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod    | value                    | property
      'tokenName'      | 'token1'                 | 'tokenName'
      'loginEndpoint'  | Mock(LoginEndpoint)      | 'loginEndpoint'
  }

  def "Setting builder properties to null values preserves existing values"() {
    given:
      def sut = new ImplicitGrantBuilder()
    when:
      sut."$builderMethod"(value)
      sut."$builderMethod"(null)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod    | value                    | property
      'tokenName'      | 'token1'                 | 'tokenName'
      'loginEndpoint'  | Mock(LoginEndpoint)      | 'loginEndpoint'
  }
}
