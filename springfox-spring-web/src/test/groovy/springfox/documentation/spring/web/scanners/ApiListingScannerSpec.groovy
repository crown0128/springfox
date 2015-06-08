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

package springfox.documentation.spring.web.scanners

import com.google.common.collect.Multimap
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import spock.lang.Unroll
import springfox.documentation.schema.mixins.SchemaPluginsSupport
import springfox.documentation.service.ApiListing
import springfox.documentation.service.ResourceGroup
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spi.service.contexts.RequestMappingContext
import springfox.documentation.spring.web.SpringGroupingStrategy
import springfox.documentation.spring.web.dummy.DummyClass
import springfox.documentation.spring.web.mixins.ApiDescriptionSupport
import springfox.documentation.spring.web.mixins.AuthSupport
import springfox.documentation.spring.web.mixins.ModelProviderForServiceSupport
import springfox.documentation.spring.web.mixins.RequestMappingSupport
import springfox.documentation.spring.web.mixins.ServicePluginsSupport
import springfox.documentation.spring.web.plugins.DocumentationContextSpec

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.collect.Maps.*
import static org.springframework.http.MediaType.*
import static springfox.documentation.builders.PathSelectors.*
import static ApiListingScanner.*

@Mixin([RequestMappingSupport, AuthSupport, ModelProviderForServiceSupport,
        ServicePluginsSupport, ApiDescriptionSupport, SchemaPluginsSupport])
class ApiListingScannerSpec extends DocumentationContextSpec {
  ApiDescriptionReader apiDescriptionReader
  ApiModelReader apiModelReader
  ApiListingScanningContext listingContext
  ApiListingScanner scanner

  def setup() {
    SecurityContext securityContext = SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(regex('/anyPath.*'))
            .build()

    contextBuilder.withResourceGroupingStrategy(new SpringGroupingStrategy())
    plugin
            .securityContexts(newArrayList(securityContext))
            .configure(contextBuilder)
    apiDescriptionReader = Mock(ApiDescriptionReader)
    apiDescriptionReader.read(_) >> []
    apiModelReader = Mock(ApiModelReader)
    apiModelReader.read(_) >> newHashMap()
    scanner = new ApiListingScanner(apiDescriptionReader, apiModelReader, defaultWebPlugins())
  }

  def "Should create an api listing for a single resource grouping "() {
    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/businesses")


      def context = context()
      RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo,
              dummyHandlerMethod("methodWithConcreteResponseBody"))
      ResourceGroup resourceGroup = new ResourceGroup("businesses", DummyClass)
      Map<ResourceGroup, List<RequestMappingContext>> resourceGroupRequestMappings = newHashMap()
      resourceGroupRequestMappings.put(resourceGroup, [requestMappingContext])
      listingContext = new ApiListingScanningContext(context, resourceGroupRequestMappings)
    when:
      apiDescriptionReader.read(requestMappingContext) >> []

    and:
      def scanned = scanner.scan(listingContext)
    then:
      scanned.containsKey("businesses")
      Collection<ApiListing> listings = scanned.get("businesses")
      listings.first().consumes == [APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE] as Set
      listings.first().produces == [APPLICATION_JSON_VALUE] as Set
      listings.first().description == 'Dummy Class'
  }

  def "should assign global authorizations"() {
    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo('/anyPath')

      def context = context()
      def requestMappingContext = new RequestMappingContext(context, requestMappingInfo,
              dummyHandlerMethod("methodWithConcreteResponseBody"))
      def resourceGroupRequestMappings = newHashMap()
      resourceGroupRequestMappings.put(new ResourceGroup("businesses", DummyClass), [requestMappingContext])

      listingContext = new ApiListingScanningContext(context, resourceGroupRequestMappings)
    when:
      Multimap<String, ApiListing> apiListingMap = scanner.scan(listingContext)
    then:
      Collection<ApiListing> listings = apiListingMap.get('businesses')
      listings.first().getSecurityReferences().size() == 0
  }

  @Unroll
  def "should find longest common path"() {
    given:
      String result = longestCommonPath(apiDescriptions(paths))

    expect:
      result == expected
    where:
      paths                                        | expected
      []                                           | null
      ['/a/b', '/a/b']                             | '/a/b'
      ['/a/b', '/a/b/c']                           | '/a/b'
      ['/a/b', '/a/']                              | '/a'
      ['/a/b', '/a/d/e/f']                         | '/a'
      ['/a/b/c/d/e/f', '/a', '/a/b']               | '/a'
      ['/d', '/e', 'f']                            | '/'
      ['/a/b/c', '/a/b/c/d/e/f', '/a/b/c/d/e/f/g'] | '/a/b/c'
  }
}
