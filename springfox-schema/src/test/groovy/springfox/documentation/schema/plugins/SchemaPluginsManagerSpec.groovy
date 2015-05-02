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

package springfox.documentation.schema.plugins
import com.fasterxml.classmate.TypeResolver
import org.springframework.plugin.core.OrderAwarePluginRegistry
import org.springframework.plugin.core.PluginRegistry
import spock.lang.Specification
import springfox.documentation.builders.ModelPropertyBuilder
import springfox.documentation.schema.AlternateTypesSupport
import springfox.documentation.schema.DefaultGenericTypeNamingStrategy
import springfox.documentation.schema.ExampleWithEnums
import springfox.documentation.schema.TypeForTestingPropertyNames
import springfox.documentation.schema.TypeNameExtractor
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.AlternateTypeProvider
import springfox.documentation.spi.schema.ModelBuilderPlugin
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin
import springfox.documentation.spi.schema.TypeNameProviderPlugin
import springfox.documentation.spi.schema.contexts.ModelContext
import springfox.documentation.spi.schema.contexts.ModelPropertyContext

import java.lang.reflect.AnnotatedElement

import static com.google.common.collect.Lists.*
import static springfox.documentation.spi.DocumentationType.*
import static springfox.documentation.spi.schema.contexts.ModelContext.inputParam

@Mixin(AlternateTypesSupport)
class SchemaPluginsManagerSpec extends Specification {
  SchemaPluginsManager sut
  TypeNameExtractor typeNames
  def propertyPlugin = Mock(ModelPropertyBuilderPlugin)
  def modelPlugin = Mock(ModelBuilderPlugin)
  def namePlugin = Mock(TypeNameProviderPlugin)

  def setup() {
    PluginRegistry<ModelPropertyBuilderPlugin, DocumentationType> propRegistry =
            OrderAwarePluginRegistry.create(newArrayList(propertyPlugin))
    propertyPlugin.supports(SPRING_WEB) >> true

    PluginRegistry<ModelBuilderPlugin, DocumentationType> modelRegistry =
            OrderAwarePluginRegistry.create(newArrayList(modelPlugin))
    modelPlugin.supports(SPRING_WEB) >> true

    PluginRegistry<TypeNameProviderPlugin, DocumentationType> modelNameRegistry =
            OrderAwarePluginRegistry.create(newArrayList(namePlugin))
    namePlugin.supports(SPRING_WEB) >> true

    sut = new SchemaPluginsManager(propRegistry, modelRegistry)
    typeNames = new TypeNameExtractor(new TypeResolver(), modelNameRegistry)
  }

  def "enriches model property when plugins are found"() {
    given:
      def context = new ModelPropertyContext(Mock(ModelPropertyBuilder), Mock(AnnotatedElement),
              new TypeResolver(), SPRING_WEB)
    when:
      sut.property(context)
    then:
      1 * propertyPlugin.apply(context)
  }

  def "enriches model when plugins are found"() {
    given:
      def namingStrategy = new DefaultGenericTypeNamingStrategy()
      def context = ModelContext.inputParam(TypeForTestingPropertyNames, SPRING_WEB, new AlternateTypeProvider([]), namingStrategy)
    and:
      context.documentationType >> SPRING_WEB
    when:
      sut.model(context)
    then:
      1 * modelPlugin.apply(context)
  }

  def "enriches model name when plugins are found"() {
    given:
      def context = inputParam(ExampleWithEnums, SPRING_WEB, alternateTypeProvider(), new DefaultGenericTypeNamingStrategy())
    and:
      context.documentationType >> SPRING_WEB
    when:
      typeNames.typeName(context)
    then:
      1 * namePlugin.nameFor(_)
  }
}
