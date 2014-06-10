package com.mangofactory.swagger.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mangofactory.swagger.models.DefaultModelPropertiesProvider;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;


public class JacksonSwaggerSupport implements ApplicationContextAware {
  private ObjectMapper springsMessageConverterObjectMapper;
  private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
  private ApplicationContext applicationContext;

  public ObjectMapper getSpringsMessageConverterObjectMapper() {
    return springsMessageConverterObjectMapper;
  }

  private Module swaggerSerializationModule() {
    SimpleModule module = new SimpleModule("SwaggerJacksonModule");
    module.addSerializer(ApiListing.class, new SwaggerApiListingJsonSerializer());
    module.addSerializer(ResourceListing.class, new SwaggerResourceListingJsonSerializer());
    return module;
  }

  @Autowired
  public void setRequestMappingHandlerAdapter(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
    this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
  }

  @PostConstruct
  public void setup() {
    List<HttpMessageConverter<?>> messageConverters = requestMappingHandlerAdapter.getMessageConverters();
    for (HttpMessageConverter<?> messageConverter : messageConverters) {
      if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter m = (MappingJackson2HttpMessageConverter) messageConverter;
        this.springsMessageConverterObjectMapper = m.getObjectMapper();
        this.springsMessageConverterObjectMapper.registerModule(swaggerSerializationModule());
      }
    }

    Map<String, DefaultModelPropertiesProvider> beans = applicationContext.getBeansOfType
            (DefaultModelPropertiesProvider.class);

    for (DefaultModelPropertiesProvider defaultModelPropertiesProvider : beans.values()) {
      defaultModelPropertiesProvider.setObjectMapper(this.springsMessageConverterObjectMapper);
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}