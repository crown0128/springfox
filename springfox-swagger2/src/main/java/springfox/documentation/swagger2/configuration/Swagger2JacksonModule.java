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

package springfox.documentation.swagger2.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.Xml;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

public class Swagger2JacksonModule extends SimpleModule implements JacksonModuleRegistrar {

  public void maybeRegisterModule(ObjectMapper objectMapper) {
    if (objectMapper.findMixInClassFor(Swagger.class) == null) {
      objectMapper.registerModule(new Swagger2JacksonModule());
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    context.setMixInAnnotations(Swagger.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Info.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(License.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Scheme.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(SecurityRequirement.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(SecuritySchemeDefinition.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Model.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Property.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Operation.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Path.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Response.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Parameter.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(ExternalDocs.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Xml.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Tag.class, CustomizedSwaggerSerializer.class);
    context.setMixInAnnotations(Contact.class, CustomizedSwaggerSerializer.class);
  }

  @JsonAutoDetect
  @JsonInclude(value = Include.NON_EMPTY)
  private class CustomizedSwaggerSerializer {
  }

}
