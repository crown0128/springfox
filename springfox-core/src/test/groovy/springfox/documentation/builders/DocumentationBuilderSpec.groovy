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

package springfox.documentation.builders
import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import spock.lang.Specification
import springfox.documentation.service.ApiListing
import springfox.documentation.service.ResourceListing

class DocumentationBuilderSpec extends Specification {
  def "Setting properties on the builder with non-null values"() {
    given:
      def sut = new DocumentationBuilder()
    when:
      sut."$builderMethod"(value)
    and:
      def built = sut.build()
    then:
      if (value instanceof Set) {
        assert built."$property".containsAll(value)
      } else if (value instanceof Multimap) {
        assert built."$property".keySet().containsAll(value.keySet())
      } else {
        assert built."$property" == value
      }

    where:
      builderMethod                     | value                          | property
      'name'                            | 'group1'                       | 'groupName'
      'apiListingsByResourceGroupName'  | multiMap()                     | 'apiListings'
      'resourceListing'                 | Mock(ResourceListing)          | 'resourceListing'
      'basePath'                        | 'urn:some-path'                | 'basePath'
      'produces'                        | ['application/json'] as Set    | 'produces'
      'consumes'                        | ['application/json'] as Set    | 'consumes'
      'schemes'                         | ['http']  as Set               | 'schemes'
      'tags'                            | ['pet'] as Set                 | 'tags'
  }

  Multimap<String, ApiListing> multiMap() {
    Multimap<String, ApiListing> multiMap = LinkedListMultimap.create()
    multiMap.put("group1", Mock(ApiListing))
    return multiMap
  }

  def "Setting builder properties to null values preserves existing values"() {
    given:
      def sut = new DocumentationBuilder()
    when:
      sut."$builderMethod"(value)
      sut."$builderMethod"(null)
    and:
      def built = sut.build()
    then:
      if (value instanceof Set) {
        assert built."$property".containsAll(value)
      } else if (value instanceof Multimap) {
        assert built."$property".keySet().containsAll(value.keySet())
      } else {
        assert built."$property" == value
      }

    where:
      builderMethod                     | value                           | property
      'name'                            | 'group1'                        | 'groupName'
      'apiListingsByResourceGroupName'  | multiMap()                      | 'apiListings'
      'resourceListing'                 | Mock(ResourceListing)           | 'resourceListing'
      'basePath'                        | 'urn:some-path'                 | 'basePath'
      'produces'                        | ['application/json'] as Set     | 'produces'
      'consumes'                        | ['application/json'] as Set     | 'consumes'
      'schemes'                         | ['http']  as Set                | 'schemes'
      'tags'                            | ['pet'] as Set                  | 'tags'
  }
}
