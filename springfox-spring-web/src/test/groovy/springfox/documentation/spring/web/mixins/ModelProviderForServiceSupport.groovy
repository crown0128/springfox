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

package springfox.documentation.spring.web.mixins

import com.fasterxml.classmate.TypeResolver
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.plugin.core.OrderAwarePluginRegistry
import org.springframework.plugin.core.PluginRegistry
import springfox.documentation.schema.*
import springfox.documentation.schema.configuration.ObjectMapperConfigured
import springfox.documentation.schema.mixins.SchemaPluginsSupport
import springfox.documentation.schema.plugins.SchemaPluginsManager
import springfox.documentation.schema.property.ObjectMapperBeanPropertyNamingStrategy
import springfox.documentation.schema.property.bean.AccessorsProvider
import springfox.documentation.schema.property.bean.BeanModelPropertyProvider
import springfox.documentation.schema.property.constructor.ConstructorModelPropertyProvider
import springfox.documentation.schema.property.field.FieldModelPropertyProvider
import springfox.documentation.schema.property.field.FieldProvider
import springfox.documentation.schema.property.DefaultModelPropertiesProvider
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.TypeNameProviderPlugin

@SuppressWarnings("GrMethodMayBeStatic")
@Mixin([ServicePluginsSupport, SchemaPluginsSupport])
class ModelProviderForServiceSupport {
  def typeNameExtractor() {
    PluginRegistry<TypeNameProviderPlugin, DocumentationType> modelNameRegistry =
        OrderAwarePluginRegistry.create([new DefaultTypeNameProvider()])
    new TypeNameExtractor(new TypeResolver(),  modelNameRegistry)
  }

  ModelProvider modelProvider(SchemaPluginsManager pluginsManager = defaultSchemaPlugins(),
                              TypeResolver typeResolver = new TypeResolver()) {

    def fields = new FieldProvider(typeResolver)
    def objectMapper = new ObjectMapper()
    def typeNameExtractor = typeNameExtractor()
    def namingStrategy = new ObjectMapperBeanPropertyNamingStrategy()
    namingStrategy.onApplicationEvent(new ObjectMapperConfigured(this, objectMapper))

    def modelPropertiesProvider = new DefaultModelPropertiesProvider(
            beanProperty(typeResolver, namingStrategy, pluginsManager, typeNameExtractor, objectMapper),
            fieldProperty(fields, namingStrategy, pluginsManager, typeNameExtractor, objectMapper),
            constructorProperty(fields, namingStrategy, pluginsManager, typeNameExtractor, objectMapper))
    def modelDependenciesProvider = new ModelDependencyProvider(typeResolver,
            modelPropertiesProvider, typeNameExtractor)
    new DefaultModelProvider(typeResolver, modelPropertiesProvider, modelDependenciesProvider,
            pluginsManager, typeNameExtractor)
  }

  def beanProperty(TypeResolver typeResolver, ObjectMapperBeanPropertyNamingStrategy namingStrategy, SchemaPluginsManager pluginsManager, TypeNameExtractor typeNameExtractor, ObjectMapper objectMapper) {
    def modelPropertyProvider = new BeanModelPropertyProvider(new AccessorsProvider(typeResolver), typeResolver
            , namingStrategy, pluginsManager, typeNameExtractor)
    modelPropertyProvider.onApplicationEvent(new ObjectMapperConfigured(this, objectMapper))
    modelPropertyProvider
  }

  def fieldProperty(FieldProvider fields, ObjectMapperBeanPropertyNamingStrategy namingStrategy, SchemaPluginsManager pluginsManager, TypeNameExtractor typeNameExtractor, ObjectMapper objectMapper) {
    def modelPropertyProvider = new FieldModelPropertyProvider(fields, namingStrategy,
            pluginsManager, typeNameExtractor)
    modelPropertyProvider.onApplicationEvent(new ObjectMapperConfigured(this, objectMapper))
    modelPropertyProvider
  }

  def constructorProperty(FieldProvider fields, ObjectMapperBeanPropertyNamingStrategy namingStrategy, SchemaPluginsManager pluginsManager, TypeNameExtractor typeNameExtractor, ObjectMapper objectMapper) {
    def modelPropertyProvider =
            new ConstructorModelPropertyProvider(fields, namingStrategy, pluginsManager, typeNameExtractor)
    modelPropertyProvider.onApplicationEvent(new ObjectMapperConfigured(this, objectMapper))
    modelPropertyProvider
  }

  ModelProvider modelProviderWithSnakeCaseNamingStrategy(SchemaPluginsManager pluginsManager = defaultSchemaPlugins(),
      TypeResolver typeResolver = new TypeResolver()) {

    def fields = new FieldProvider(typeResolver)
    def objectMapper = new ObjectMapper()
    def typeNameExtractor = typeNameExtractor()
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
    def namingStrategy = new ObjectMapperBeanPropertyNamingStrategy()
    namingStrategy.onApplicationEvent(new ObjectMapperConfigured(this, objectMapper))

    def modelPropertiesProvider = new DefaultModelPropertiesProvider(
            beanProperty(typeResolver, namingStrategy, pluginsManager, typeNameExtractor, objectMapper),
            fieldProperty(fields, namingStrategy, pluginsManager, typeNameExtractor, objectMapper),
            constructorProperty(fields, namingStrategy, pluginsManager, typeNameExtractor, objectMapper))
    def modelDependenciesProvider = new ModelDependencyProvider(typeResolver,
            modelPropertiesProvider, typeNameExtractor)
    new DefaultModelProvider(typeResolver, modelPropertiesProvider, modelDependenciesProvider,
            pluginsManager, typeNameExtractor)
  }


}
