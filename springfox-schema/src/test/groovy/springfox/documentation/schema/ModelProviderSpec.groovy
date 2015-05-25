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

package springfox.documentation.schema

import com.fasterxml.classmate.TypeResolver
import org.springframework.http.HttpHeaders
import spock.lang.Specification
import spock.lang.Unroll
import springfox.documentation.schema.mixins.ModelProviderSupport
import springfox.documentation.schema.mixins.TypesForTestingSupport

import static springfox.documentation.spi.DocumentationType.*
import static springfox.documentation.spi.schema.contexts.ModelContext.*

@Mixin([TypesForTestingSupport, ModelProviderSupport, AlternateTypesSupport])
class ModelProviderSpec extends Specification {

  def namingStrategy = new DefaultGenericTypeNamingStrategy()
  def "dependencies provider respects ignorables"() {
    given:
      ModelProvider sut = defaultModelProvider()
      def context = inputParam(modelType, SWAGGER_12, alternateTypeProvider(), namingStrategy)
      context.seen(new TypeResolver().resolve(HttpHeaders))
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
      def dependentTypeNames = provider.dependencies(inputParam(modelType, SWAGGER_12,
              alternateTypeProvider(), namingStrategy)).keySet().sort()

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
      genericClassWithGenericField() | ["HttpHeaders", "ResponseEntityAlternative«SimpleType»", "SimpleType"].sort()
      genericClassWithDeepGenerics() | ["HttpHeaders", "ResponseEntityAlternative«List«SimpleType»»", "SimpleType"].sort()
      genericCollectionWithEnum()    | ["Collection«string»"]
      recursiveType()                | ["SimpleType"]
  }
}
