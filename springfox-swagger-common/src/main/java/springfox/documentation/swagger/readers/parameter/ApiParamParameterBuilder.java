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

package springfox.documentation.swagger.readers.parameter;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.Enums;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.schema.ApiModelProperties;

import static com.google.common.base.Strings.*;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.*;

@Component("swaggerParameterDescriptionReader")
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class ApiParamParameterBuilder implements ParameterBuilderPlugin {
  private final DescriptionResolver descriptions;

  @Autowired
  public ApiParamParameterBuilder(DescriptionResolver descriptions) {
    this.descriptions = descriptions;
  }

  @Override
  public void apply(ParameterContext context) {
    Optional<ApiParam> apiParam = context.resolvedMethodParameter().findAnnotation(ApiParam.class);
    context.parameterBuilder()
        .allowableValues(allowableValues(
            context.alternateFor(context.resolvedMethodParameter().getParameterType()),
            apiParam.transform(toAllowableValue()).or("")));
    if (apiParam.isPresent()) {
      ApiParam annotation = apiParam.get();
      context.parameterBuilder().name(emptyToNull(annotation.name()));
      context.parameterBuilder().description(emptyToNull(descriptions.resolve(annotation.value())));
      context.parameterBuilder().parameterAccess(emptyToNull(annotation.access()));
      context.parameterBuilder().defaultValue(emptyToNull(annotation.defaultValue()));
      context.parameterBuilder().allowMultiple(annotation.allowMultiple());
      context.parameterBuilder().required(annotation.required());
      context.parameterBuilder().hidden(annotation.hidden());
    }
  }

  private Function<ApiParam, String> toAllowableValue() {
    return new Function<ApiParam, String>() {
      @Override
      public String apply(ApiParam input) {
        return input.allowableValues();
      }
    };
  }

  private AllowableValues allowableValues(ResolvedType parameterType, String allowableValueString) {
    AllowableValues allowableValues = null;
    if (!isNullOrEmpty(allowableValueString)) {
      allowableValues = ApiModelProperties.allowableValueFromString(allowableValueString);
    } else {
      if (parameterType.getErasedType().isEnum()) {
        allowableValues = Enums.allowableValues(parameterType.getErasedType());
      }
      if (Collections.isContainerType(parameterType)) {
        allowableValues = Enums.allowableValues(Collections.collectionElementType(parameterType).getErasedType());
      }
    }
    return allowableValues;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
