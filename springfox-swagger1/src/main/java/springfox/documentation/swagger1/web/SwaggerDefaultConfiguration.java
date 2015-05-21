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

package springfox.documentation.swagger1.web;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DefaultsProviderPlugin;
import springfox.documentation.spi.service.contexts.Defaults;
import springfox.documentation.spi.service.contexts.DocumentationContextBuilder;
import springfox.documentation.spring.web.plugins.DefaultConfiguration;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static springfox.documentation.schema.AlternateTypeRules.*;

@Component
public class SwaggerDefaultConfiguration implements DefaultsProviderPlugin {

  private final DefaultConfiguration defaultConfiguration;
  private TypeResolver typeResolver;

  @Autowired
  public SwaggerDefaultConfiguration(Defaults defaults, TypeResolver typeResolver, ServletContext servletContext) {
    this.typeResolver = typeResolver;
    defaultConfiguration = new DefaultConfiguration(defaults, typeResolver, servletContext);
  }

  @Override
  public DocumentationContextBuilder create(DocumentationType documentationType) {
    List<AlternateTypeRule> rules = newArrayList();
    rules.add(newRule(typeResolver.resolve(Map.class, String.class, String.class),
        typeResolver.resolve(Object.class)));
    rules.add(newMapRule(WildcardType.class, WildcardType.class));
    return defaultConfiguration.create(documentationType)
        .rules(rules);
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return DocumentationType.SWAGGER_12.equals(delimiter);
  }
}
