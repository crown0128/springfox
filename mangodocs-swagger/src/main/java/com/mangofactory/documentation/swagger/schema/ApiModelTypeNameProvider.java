package com.mangofactory.documentation.swagger.schema;

import com.mangofactory.documentation.spi.DocumentationType;
import com.mangofactory.documentation.schema.DefaultTypeNameProvider;
import com.wordnik.swagger.annotations.ApiModel;
import org.springframework.stereotype.Component;

import static com.google.common.base.Optional.*;
import static com.google.common.base.Strings.*;
import static com.mangofactory.documentation.swagger.common.SwaggerPluginSupport.*;
import static org.springframework.core.annotation.AnnotationUtils.*;

@Component
public class ApiModelTypeNameProvider extends DefaultTypeNameProvider {
  @Override
  public String nameFor(Class<?> type) {
    ApiModel annotation = findAnnotation(type, ApiModel.class);
    String defaultTypeName = super.nameFor(type);
    if (annotation != null) {
      return fromNullable(emptyToNull(annotation.value())).or(defaultTypeName);
    }
    return defaultTypeName;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
