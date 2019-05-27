/*
 *
 *  Copyright 2017-2019 the original author or authors.
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
package springfox.test.contract.swaggertests

import com.fasterxml.classmate.TypeResolver
import org.joda.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.hateoas.Link
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.http.HttpMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.AuthorizationScopeBuilder
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.SecurityScheme
import springfox.documentation.service.StringVendorExtension
import springfox.documentation.service.Tag
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.ApiListingScannerPlugin
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration
import springfox.documentation.spring.web.dummy.controllers.BugsController
import springfox.documentation.spring.web.dummy.controllers.FeatureDemonstrationService
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc
import springfox.petstore.PetStoreConfiguration
import springfox.test.contract.swagger.Bug1767ListingScanner

import java.nio.ByteBuffer

import static java.util.Collections.*
import static java.util.function.Predicate.*
import static springfox.documentation.builders.PathSelectors.*
import static springfox.documentation.schema.AlternateTypeRules.*

@Configuration
@EnableSwagger2WebMvc
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@Import([SpringDataRestConfiguration, PetStoreConfiguration])
class Swagger2TestConfig {

  @Autowired
  private TypeResolver resolver

  @Bean
  Docket petstoreWithUriTemplating(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("petstoreTemplated")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/api/store/search.*"))
        .build()
        .enableUrlTemplating(true)
        .host("petstore.swagger.io")
        .protocols(['http', 'https'] as Set)
  }

  @Bean
  Docket business(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("businessService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/business.*"))
        .build()
  }

  @Bean
  Docket concrete(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("concrete")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/foo/.*"))
        .build()
  }

  @Bean
  Docket noRequestMapping(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("noRequestMapping")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/no-request-mapping/.*"))
        .build()
  }

  @Bean
  Docket fancyPetstore(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("fancyPetstore")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/fancypets/.*"))
        .build()
  }

  @Bean
  Docket featureService(List<SecurityScheme> authorizationTypes) {
    // tag::question-27-config[]
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("featureService")
        .useDefaultResponseMessages(false)
        .additionalModels(resolver.resolve(FeatureDemonstrationService.CustomTypeFor2031.class)) // <1>
    // end::question-27-config[]
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .alternateTypeRules(newRule(LocalDate.class, String.class))
        .select()
        .paths(regex("/features/.*"))
        .build()
  }

  @Bean
  public Docket inheritedService(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("inheritedService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/child/.*"))
        .build()
  }

  @Bean
  Docket pet(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("petService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .enableUrlTemplating(true)
        .select()
        .paths(regex("/pets/.*"))
        .build()
  }

  @Bean
  Docket bugs(List<SecurityScheme> authorizationTypes) {
    AuthorizationScope[] scopes = [new AuthorizationScopeBuilder()
                                       .scope("read")
                                       .description("Read access")
                                       .build()]
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("bugs")
        .apiInfo(new ApiInfoBuilder().version("1.0")
          .title("bugs API")
          .description("bugs API")
          .extensions(
             singletonList(
                new StringVendorExtension("test", "testValue")))
          .build())
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .tags(new Tag("foo", "Foo Description"))
        .produces(['application/xml', 'application/json'] as Set)
        .enableUrlTemplating(true)
        .securityContexts(
        [SecurityContext.builder()
             .securityReferences([new SecurityReference("petstore_auth", scopes)])
             .forPaths(regex("/bugs/2268"))
             .forHttpMethods(isEqual(HttpMethod.GET))
             .build()
        ])
        .alternateTypeRules(
          newRule(URL.class, String.class),
          newRule(
            resolver.resolve(List.class, Link.class),
            resolver.resolve(Map.class, String.class, BugsController.LinkAlternate.class)))
        .directModelSubstitute(ByteBuffer.class, String.class)
        .select()
          .paths(regex("/bugs/.*"))
        .build()
  }

  @Bean
  Docket bugsDifferent(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("bugsDifferent")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .tags(new Tag("foo", "Foo Description"))
        .produces(['application/xml', 'application/json'] as Set)
        .enableUrlTemplating(true)
        .alternateTypeRules(
        newRule(URL.class, String.class),
        newRule(
            resolver.resolve(List.class, Link.class),
            resolver.resolve(Map.class, String.class, BugsController.LinkAlternate.class)))
        .directModelSubstitute(ByteBuffer.class, String.class)
        .ignoredParameterTypes(BugsController.Bug1627, BugsController.Lang)
        .select()
        .paths(regex("/bugs/.*"))
        .build()
  }

  @Bean
  Docket differentGroup() {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("different-group")
        .useDefaultResponseMessages(false)
        .tags(new Tag("Different", "Different Group"))
        .produces(['application/xml', 'application/json'] as Set)
        .enableUrlTemplating(true)
        .alternateTypeRules(
        newRule(URL.class, String.class),
        newRule(
            resolver.resolve(List.class, Link.class),
            resolver.resolve(Map.class, String.class, BugsController.LinkAlternate.class)))
        .directModelSubstitute(ByteBuffer.class, String.class)
        .select()
        .paths(regex("/different/.*"))
        .build()
  }

  @Bean
  Docket petGrooming(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("petGroomingService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/petgrooming/.*"))
        .build()
  }

  @Bean
  Docket root(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("root")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .ignoredParameterTypes(MetaClass)
        .select()
        .paths(regex("/.*"))
        .build()
  }

  @Bean
  Docket groovyServiceBean(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("groovyService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .forCodeGeneration(true)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/groovy/.*"))
        .build()
        .ignoredParameterTypes(MetaClass)
  }

  @Bean
  Docket enumServiceBean(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("enumService")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/enums/.*"))
        .build()
  }

  @Bean
  Docket featureServiceForCodeGen(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("featureService-codeGen")
        .additionalModels(resolver.resolve(FeatureDemonstrationService.CustomTypeFor2031.class))
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .forCodeGeneration(true)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/features/.*"))
        .build()
  }

  @Bean
  Docket consumesProducesNotOnDocumentContext(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("consumesProducesNotOnDocumentContext")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .select()
        .paths(regex("/consumes-produces/.*"))
        .build()
  }

  @Bean
  Docket consumesProducesOnDocumentContext(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("consumesProducesOnDocumentContext")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .consumes(['text/plain'] as Set)
        .produces(['application/json'] as Set)
        .select()
        .paths(regex("/consumes-produces/.*"))
        .build()
  }

  @Bean
  ApiListingScannerPlugin listingScanner(CachingOperationNameGenerator operationNames) {
    new Bug1767ListingScanner(operationNames)
  }

  @Bean
  Docket springDataRest() {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("spring-data-rest")
        .useDefaultResponseMessages(false)
        .enableUrlTemplating(true)
        .securitySchemes([])
        .forCodeGeneration(true)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(
          regex("/rest/people.*")
          .or(regex("/rest/tags.*"))
          .or(regex("/rest/categories.*"))
          .or(regex("/rest/addresses.*")))
        .build()
  }

  @Bean
  public Docket same(List<SecurityScheme> authorizationTypes) {
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName("same")
        .useDefaultResponseMessages(false)
        .securitySchemes(authorizationTypes)
        .produces(['application/xml', 'application/json'] as Set)
        .select()
        .paths(regex("/same/.*"))
        .build()

  }
}
