package com.mangofactory.test.contract.swagger
import com.mangofactory.documentation.service.AuthorizationType
import com.mangofactory.documentation.spi.DocumentationType
import com.mangofactory.documentation.spring.web.plugins.DocumentationConfigurer
import com.mangofactory.documentation.swagger2.annotations.EnableSwagger2
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static groovyx.net.http.ContentType.*

@ContextConfiguration(loader = SpringApplicationContextLoader,
        classes = SwaggerV2_0Spec.Config)
@WebAppConfiguration
@IntegrationTest("server.port:8080")
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
class SwaggerV2_0Spec extends Specification implements FileAccess {

  @Value('${local.server.port}')
  int port;

  @Unroll("#groupName")
  def 'should honor swagger resource listing'() {
    given:
      RESTClient http = new RESTClient("http://localhost:$port")
      String contract = fileContents("/contract/swagger2/$contractFile")

    when:
      def response = http.get(
              path: '/v2/api-docs',
              query: [group: groupName],
              contentType: TEXT, //Allows to access the raw response body
              headers: [Accept: 'application/json']
      )
    then:
      String raw = response.data.text
      String actual = JsonOutput.prettyPrint(raw)
      response.status == 200
//      println(actual)

      JSONAssert.assertEquals(contract, actual, JSONCompareMode.NON_EXTENSIBLE)

    where:
      contractFile                                                  | groupName
      'swagger.json'                                                | 'petstore'
      'declaration-business-service.json'                           | 'businessService'
      'declaration-concrete-controller.json'                        | 'concrete'
      'declaration-controller-with-no-request-mapping-service.json' | 'noRequestMapping'
      'declaration-fancy-pet-service.json'                          | 'fancyPetstore'
      'declaration-feature-demonstration-service.json'              | 'featureService'
      'declaration-inherited-service-impl.json'                     | 'inheritedService'
      'declaration-pet-grooming-service.json'                       | 'petGroomingService'
      'declaration-pet-service.json'                                | 'petService'
//      'declaration-root-controller.json'                            | 'root'
  }

  @Configuration
  @EnableSwagger2
  @ComponentScan([
          "com.mangofactory.documentation.spring.web.dummy.controllers",
          "com.mangofactory.test.contract.swagger",
          "com.mangofactory.petstore.controller"
  ])
  @Import(AuthorizationSupport)
  static class Config {
    @Bean
    public DocumentationConfigurer petstore(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("petstore")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/api/.*");
    }

    @Bean
    public DocumentationConfigurer business(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("businessService")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/business.*");
    }

    @Bean
    public DocumentationConfigurer concrete(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("concrete")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/foo/.*");
    }

    @Bean
    public DocumentationConfigurer noRequestMapping(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("noRequestMapping")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/no-request-mapping/.*");
    }

    @Bean
    public DocumentationConfigurer fancyPetstore(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("fancyPetstore")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/fancypets/.*");
    }

    @Bean
    public DocumentationConfigurer featureService(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("featureService")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/features/.*");
    }

    @Bean
    public DocumentationConfigurer inheritedService(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("inheritedService")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/child/.*");
    }

    @Bean
    public DocumentationConfigurer pet(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("petService")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/pets/.*");
    }

    @Bean
    public DocumentationConfigurer petGrooming(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("petGroomingService")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/petgrooming/.*");
    }

    @Bean
    public DocumentationConfigurer root(List<AuthorizationType> authorizationTypes) {
      return new DocumentationConfigurer(DocumentationType.SWAGGER_2)
              .groupName("root")
              .useDefaultResponseMessages(false)
              .authorizationTypes(authorizationTypes)
              .produces(['application/xml', 'application/json'] as Set)
              .includePatterns("/.*");
    }
  }
}
