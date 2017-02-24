/*
 *
 *  Copyright 2016-2017 the original author or authors.
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
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.util.MinMaxUtil;
import springfox.documentation.service.AllowableRangeValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.annotation.Annotation;

@Component
@Order(BeanValidators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class ParameterMinMaxAnnotationPlugin implements ParameterBuilderPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(ParameterMinMaxAnnotationPlugin.class);

  public static <T extends Annotation> Optional<T> validatorFromField(ParameterContext context, Class<T>
      annotationType) {

    MethodParameter methodParam = context.methodParameter();
    LOG.debug("methodParam.index: {}", methodParam.getParameterIndex());
    LOG.debug("methodParam.name: {}", methodParam.getParameterName());
    T annotatedElement = methodParam.getParameterAnnotation(annotationType);
    Optional<T> notNull = Optional.absent();
    if (annotatedElement != null) {
      notNull = Optional.fromNullable(annotatedElement);
    }
    return notNull;
  }

  public boolean supports(DocumentationType delimiter) {
    // we simply support all documentationTypes!
    return true;
  }

  public void apply(ParameterContext context) {
    Optional<Min> min = extractMin(context);
    Optional<Max> max = extractMax(context);
    if (min.isPresent() || max.isPresent()) {
      AllowableRangeValues values = MinMaxUtil.createAllowableValuesFromMinMaxForNumbers(min, max);
      LOG.debug("adding allowable Values: " + values.getMin() + " - " + values.getMax());
      context.parameterBuilder().allowableValues(values);

      // TODO Additionally show @Min/@Max in the description until
      // https://github.com/springfox/springfox/issues/1244 gets fixed
      context.parameterBuilder().description("@Min: " + values.getMin() + " - @Max: " + values.getMax() + " (until "
          + "#1244 gets fixed)");
    }
  }

  @VisibleForTesting
  Optional<Min> extractMin(ParameterContext context) {
    return validatorFromField(context, Min.class);
  }

  @VisibleForTesting
  Optional<Max> extractMax(ParameterContext context) {
    return validatorFromField(context, Max.class);
  }
}
