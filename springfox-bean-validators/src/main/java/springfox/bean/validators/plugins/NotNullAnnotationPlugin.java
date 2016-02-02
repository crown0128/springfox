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
package springfox.bean.validators.plugins;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.service.AllowableRangeValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Component
@Order(BeanValidators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class NotNullAnnotationPlugin implements ModelPropertyBuilderPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(NotNullAnnotationPlugin.class);

  @Override
  public boolean supports(DocumentationType delimiter) {
    // we simply support all documentationTypes!
    return true;
  }

  @Override
  public void apply(ModelPropertyContext context) {
    ModelPropertyBuilder mybuilder = context.getBuilder();

    Optional<BeanPropertyDefinition> beanPropDef = context.getBeanPropertyDefinition();
    BeanPropertyDefinition beanDef = beanPropDef.get();
    AnnotatedField field = beanDef.getField();

    if (field != null) {

      // add support for @NotNull
      addRequiredForNotNull(mybuilder, field);

      AllowableValues myvalues = null;

      // add support for @Size
      Size mySize = field.getAnnotation(Size.class);

      if (mySize != null) {
        myvalues = createAllowableValuesFromSizeForStrings(field, mySize);

      } else {
        // add support for @Min/@Max
        Min myMin = field.getAnnotation(Min.class);
        Max myMax = field.getAnnotation(Max.class);
        if (myMin != null || myMax != null) {
          myvalues = createAllowableValuesFromMinMaxForNumbers(field, myMin, myMax);
        } else {

        }

      }

      if (myvalues != null) {
        mybuilder.allowableValues(myvalues);
      }


    }


  }

  private void addRequiredForNotNull(ModelPropertyBuilder mybuilder, AnnotatedField field) {
    // add support for @NotNull
    NotNull myNotNull = field.getAnnotation(NotNull.class);
    if (myNotNull != null) {
      LOG.debug("@NotNull detected: adding required to field " + field.getFullName());
      mybuilder.required(true);
    }
  }

  private AllowableValues createAllowableValuesFromSizeForStrings(AnnotatedField field, Size mySize) {
    AllowableRangeValues myvalues = null;

    LOG.debug("@Size detected: adding MinLength/MaxLength to field " + field.getFullName());

    if (mySize.min() > 0 && mySize.max() < Integer.MAX_VALUE) {
      myvalues = new AllowableRangeValues(Integer.toString(mySize.min()), Integer.toString(mySize.max()));

    } else if (mySize.min() > 0) {
      LOG.debug("@Size min detected: adding AllowableRangeValues to field " + field.getFullName());
      // use Max value until "infinity" works
      myvalues = new AllowableRangeValues(Integer.toString(mySize.min()), Integer.toString(Integer.MAX_VALUE));

    } else if (mySize.max() < Integer.MAX_VALUE) {
      // use Min value until "infinity" works
      LOG.debug("@Size max detected: adding AllowableRangeValues to field " + field.getFullName());
      myvalues = new AllowableRangeValues(Integer.toString(0), Integer.toString(mySize.max()));
    }

    return myvalues;
  }

  private AllowableValues createAllowableValuesFromMinMaxForNumbers(AnnotatedField field, Min myMin, Max myMax) {
    AllowableRangeValues myvalues = null;

    if (myMin != null && myMax != null) {
      LOG.debug("@Min+@Max detected: adding AllowableRangeValues to field " + field.getFullName());
      myvalues = new AllowableRangeValues(Double.toString(myMin.value()), Double.toString(myMax.value()));

    } else if (myMin != null) {
      LOG.debug("@Min detected: adding AllowableRangeValues to field " + field.getFullName());
      // use Max value until "infinity" works
      myvalues = new AllowableRangeValues(Double.toString(myMin.value()), Double.toString(Double.MAX_VALUE));

    } else if (myMax != null) {
      // use Min value until "infinity" works
      LOG.debug("@Max detected: adding AllowableRangeValues to field " + field.getFullName());
      myvalues = new AllowableRangeValues(Double.toString(Double.MIN_VALUE), Double.toString(myMax.value()));

    }
    return myvalues;
  }


}
