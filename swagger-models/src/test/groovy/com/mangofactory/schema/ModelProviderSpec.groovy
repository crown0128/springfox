package com.mangofactory.schema

import com.fasterxml.classmate.TypeResolver
import com.mangofactory.swagger.mixins.ModelProviderSupport
import com.mangofactory.swagger.mixins.TypesForTestingSupport
import org.springframework.http.HttpHeaders
import spock.lang.Specification
import spock.lang.Unroll

import static com.mangofactory.schema.ResolvedTypes.*

@Mixin([TypesForTestingSupport, ModelProviderSupport])
class ModelProviderSpec extends Specification {

  def "dependencies provider respects ignorables"() {
    given:
      ModelProvider sut = defaultModelProvider()
      def context = ModelContext.inputParam(modelType, documentationType())
      context.seen(asResolved(new TypeResolver(), HttpHeaders))
      def dependentTypeNames = sut.dependencies(context).keySet().sort()

    expect:
      dependencies == dependentTypeNames

    where:
      modelType                      | dependencies
      genericClassWithGenericField() | ["ResponseEntityAlternative«SimpleType»", "SimpleType"].sort()
  }

  @Unroll
  def "dependencies are inferred correctly by the model provider"() {
    given:
      ModelProvider provider = defaultModelProvider()
      def dependentTypeNames = provider.dependencies(ModelContext.inputParam(modelType, documentationType())).keySet().sort()

    expect:
      dependencies == dependentTypeNames

    where:
      modelType                      | dependencies
      simpleType()                   | []
      complexType()                  | ["Category"]
      inheritedComplexType()         | ["Category"]
      typeWithLists()                | ["Category", "ComplexType"].sort()
      typeWithSets()                 | ["Category", "ComplexType"].sort()
      typeWithArrays()               | ["Category", "ComplexType"]
      genericClass()                 | ["SimpleType"]
      genericClassWithListField()    | ["SimpleType"]
      genericClassWithGenericField() | ["Charset", "Entry«string,string»", "HttpHeaders", "MediaType", "ResponseEntityAlternative«SimpleType»", "SimpleType", "URI"].sort()
      genericClassWithDeepGenerics() | ["Charset", "Entry«string,string»", "HttpHeaders", "MediaType", "ResponseEntityAlternative«List«SimpleType»»", "SimpleType", "URI"].sort()
      genericCollectionWithEnum()    | ["Collection«string»"]
      recursiveType()                | ["SimpleType"]
  }
}
