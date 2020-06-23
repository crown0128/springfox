/*
 *
 *  Copyright 2016 the original author or authors.
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
package springfox.bean.validators.plugins.schema

import com.fasterxml.classmate.TypeResolver
import spock.lang.Specification
import spock.lang.Unroll
import springfox.bean.validators.plugins.models.DecimalMinMaxTestModel
import springfox.documentation.builders.ModelPropertyBuilder
import springfox.documentation.builders.PropertySpecificationBuilder
import springfox.documentation.schema.NumericElementFacet
import springfox.documentation.service.AllowableRangeValues
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.contexts.ModelPropertyContext

class DecimalMinMaxAnnotationPluginSpec extends Specification {
  def "Always supported"() {
    expect:
    new DecimalMinMaxAnnotationPlugin().supports(types)

    where:
    types << [DocumentationType.SPRING_WEB, DocumentationType.SWAGGER_2, DocumentationType.SWAGGER_12]
  }

  @Unroll
  def "@DecimalMin/@DecimalMax annotations are reflected in the model #propertyName that are AnnotatedElements"() {
    given:
    def sut = new DecimalMinMaxAnnotationPlugin()
    def element = DecimalMinMaxTestModel.getDeclaredField(propertyName)
    def context = new ModelPropertyContext(
        new ModelPropertyBuilder(),
        new PropertySpecificationBuilder(propertyName),
        element,
        new TypeResolver(),
        DocumentationType.SWAGGER_12)

    when:
    sut.apply(context)
    def property = context.builder.build()
    def numericRange = context.getSpecificationBuilder().build()
        ?.facetOfType(NumericElementFacet)
        ?.orElse(null)

    then:
    def range = property.allowableValues as AllowableRangeValues
    range?.max == expectedMax
    range?.exclusiveMax == exclusiveMax
    range?.min == expectedMin
    range?.exclusiveMin == exclusiveMin

    and:
    numericRange?.maximum == expectedMax ?: new BigDecimal(expectedMax)
    numericRange?.exclusiveMaximum == exclusiveMax
    numericRange?.minimum == expectedMin ?: new BigDecimal(expectedMin)
    numericRange?.exclusiveMinimum  == exclusiveMin

    where:
    propertyName    | expectedMin | exclusiveMin | expectedMax | exclusiveMax
    "noAnnotation"  | null        | null         | null        | null
    "onlyMin"       | "10.5"      | false        | null        | null
    "onlyMax"       | null        | null         | "20.5"      | false
    "both"          | "10.5"      | false        | "20.5"      | false
    "minExclusive"  | "10.5"      | true         | null        | null
    "maxExclusive"  | null        | null         | "20.5"      | true
    "bothExclusive" | "10.5"      | true         | "20.5"      | true
  }
}
