/*
 *
 *  Copyright 2015-2019 the original author or authors.
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
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.EnumerationElementFacetBuilder;
import springfox.documentation.builders.ExampleBuilder;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.Enums;
import springfox.documentation.schema.Example;
import springfox.documentation.schema.NumericElementFacetBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableRangeValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.service.CollectionFormat;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.schema.ApiModelProperties;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.isEmpty;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;
import static springfox.documentation.swagger.readers.parameter.Examples.allExamples;
import static springfox.documentation.swagger.readers.parameter.Examples.examples;

@Component("swaggerParameterDescriptionReader")
@Order(SWAGGER_PLUGIN_ORDER)
public class ApiParamParameterBuilder implements ParameterBuilderPlugin {
  private final DescriptionResolver descriptions;
  private final EnumTypeDeterminer enumTypeDeterminer;

  @Autowired
  public ApiParamParameterBuilder(
      DescriptionResolver descriptions,
      EnumTypeDeterminer enumTypeDeterminer) {
    this.descriptions = descriptions;
    this.enumTypeDeterminer = enumTypeDeterminer;
  }

  @Override
  public void apply(ParameterContext context) {
    Optional<ApiParam> apiParam = context.resolvedMethodParameter().findAnnotation(ApiParam.class);
    AllowableValues allowedValues = allowableValues(
        context.alternateFor(context.resolvedMethodParameter().getParameterType()),
        apiParam.map(ApiParam::allowableValues).orElse(""));

    context.parameterBuilder()
        .allowableValues(allowedValues);

    if (allowedValues instanceof AllowableListValues) {
      context.requestParameterBuilder()
          .simpleParameterBuilder()
          .facetBuilder(EnumerationElementFacetBuilder.class)
          .allowedValues(allowedValues);
    } else if (allowedValues instanceof AllowableRangeValues) {
      context.requestParameterBuilder()
          .simpleParameterBuilder()
          .facetBuilder(NumericElementFacetBuilder.class)
          .from((AllowableRangeValues) allowedValues);
    }

    if (apiParam.isPresent()) {
      ApiParam annotation = apiParam.get();
      Example example = null;
      if (annotation.example().length() > 0) {
        example = new ExampleBuilder()
            .value(annotation.example())
            .build();
      }
      context.parameterBuilder()
          .name(ofNullable(annotation.name())
              .filter(((Predicate<String>) String::isEmpty).negate()).orElse(null))
          .description(ofNullable(descriptions.resolve(annotation.value()))
              .filter(((Predicate<String>) String::isEmpty).negate()).orElse(null))
          .parameterAccess(ofNullable(annotation.access())
              .filter(((Predicate<String>) String::isEmpty).negate())
              .orElse(null))
          .defaultValue(ofNullable(annotation.defaultValue())
              .filter(((Predicate<String>) String::isEmpty).negate())
              .orElse(null))
          .allowMultiple(annotation.allowMultiple())
          .allowEmptyValue(annotation.allowEmptyValue())
          .required(annotation.required())
          .scalarExample(example)
          .complexExamples(examples(annotation.examples()))
          .hidden(annotation.hidden())
          .collectionFormat(annotation.collectionFormat())
          .order(SWAGGER_PLUGIN_ORDER);
      context.requestParameterBuilder()
          .name(
              annotation.name().isEmpty()
                  ? null
                  : annotation.name())
          .description(ofNullable(descriptions.resolve(annotation.value()))
              .filter(desc -> !desc.isEmpty())
              .orElse(null))
          .required(annotation.required())
          .hidden(annotation.hidden())
          .precedence(SWAGGER_PLUGIN_ORDER)
          .simpleParameterBuilder()
          .collectionFormat(CollectionFormat.convert(annotation.collectionFormat()).orElse(null))
          .defaultValue(
              annotation.defaultValue().isEmpty()
                  ? null
                  : annotation.defaultValue())
          .allowEmptyValue(annotation.allowEmptyValue())
          .yield()
          .example(example)
          .examples(allExamples(annotation.examples()));
    }
  }

  private AllowableValues allowableValues(ResolvedType parameterType, String allowableValueString) {
    AllowableValues allowableValues = null;
    if (!isEmpty(allowableValueString)) {
      allowableValues = ApiModelProperties.allowableValueFromString(allowableValueString);
    } else {
      if (enumTypeDeterminer.isEnum(parameterType.getErasedType())) {
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
