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
package springfox.bean.validators.plugins;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.util.MinMaxUtil;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static springfox.bean.validators.plugins.BeanValidators.*;

@Component
@Order(BeanValidators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class ModelPropertyMinMaxAnnotationPlugin implements ModelPropertyBuilderPlugin {

  /**
   * support all documentationTypes
   */
  @Override
  public boolean supports(DocumentationType delimiter) {
    // we simply support all documentationTypes!
    return true;
  }

  /**
   * read Min/Max annotations
   */
  @Override
  public void apply(ModelPropertyContext context) {
    Optional<Min> min = extractMin(context);
    Optional<Max> max = extractMax(context);

    // add support for @Min/@Max
    context.getBuilder().allowableValues(MinMaxUtil.createAllowableValuesFromMinMaxForNumbers(min, max));

  }

  /**
   * extract Min from bean or field
   *
   * @param context
   * @return
   */
  @VisibleForTesting
  Optional<Min> extractMin(ModelPropertyContext context) {
    return validatorFromBean(context, Min.class).or(validatorFromField(context, Min.class));
  }

  /**
   * extract Min from bean or field
   *
   * @param context
   * @return
   */
  @VisibleForTesting
  Optional<Max> extractMax(ModelPropertyContext context) {
    return validatorFromBean(context, Max.class).or(validatorFromField(context, Max.class));
  }
}
