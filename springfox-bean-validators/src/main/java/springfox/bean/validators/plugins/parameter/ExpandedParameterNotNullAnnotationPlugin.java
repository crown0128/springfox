/*
 *
 *  Copyright 2015-2017 the original author or authors.
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
package springfox.bean.validators.plugins.parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.plugins.Validators;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
@Order(Validators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class ExpandedParameterNotNullAnnotationPlugin implements ExpandedParameterBuilderPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(ExpandedParameterNotNullAnnotationPlugin.class);

  @Override
  public boolean supports(DocumentationType delimiter) {
    // we simply support all documentationTypes!
    return true;
  }

  @Override
  public void apply(ParameterExpansionContext context) {

    Optional<NotNull> notNull = context.findAnnotation(NotNull.class);

    if (notNull.isPresent()) {
      LOG.debug("Setting parameter to required because of @NotNull attribute");
      context.getParameterBuilder().required(true);
      context.getRequestParameterBuilder().required(true);
    }
  }
}
