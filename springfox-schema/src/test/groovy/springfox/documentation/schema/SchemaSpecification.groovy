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
import org.springframework.plugin.core.OrderAwarePluginRegistry
import org.springframework.plugin.core.PluginRegistry
import spock.lang.Specification
import springfox.documentation.schema.mixins.ModelProviderSupport
import springfox.documentation.schema.mixins.SchemaPluginsSupport
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.TypeNameProviderPlugin

@Mixin([ModelProviderSupport, SchemaPluginsSupport])
class SchemaSpecification extends Specification {
  TypeNameExtractor typeNameExtractor
  ModelProvider modelProvider
  ModelDependencyProvider modelDependencyProvider
  DocumentationType documentationType = DocumentationType.SWAGGER_12
  def setup() {
    PluginRegistry<TypeNameProviderPlugin, DocumentationType> modelNameRegistry =
        OrderAwarePluginRegistry.create([new DefaultTypeNameProvider()])
    typeNameExtractor =
            new TypeNameExtractor(new TypeResolver(), modelNameRegistry)
    modelProvider = defaultModelProvider()
    modelDependencyProvider = defaultModelDependencyProvider()
  }
}
