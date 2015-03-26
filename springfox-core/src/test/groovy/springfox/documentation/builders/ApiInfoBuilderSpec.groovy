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

import spock.lang.Specification

class ApiInfoBuilderSpec extends Specification {
  def "Setting properties on the builder with non-null values"() {
    given:
      def sut = new ApiInfoBuilder()
    when:
      sut."$builderMethod"(value)
    and:
      def built = sut.build()
    then:
      built."$property" == value

    where:
      builderMethod       | value                   | property
      'version'           | '1.0'                   | 'version'
      'title'             | 'title'                 | 'title'
      'termsOfServiceUrl' | 'urn:tos'               | 'termsOfServiceUrl'
      'description'       | 'test'                  | 'description'
      'contact'           | 'Contact'               | 'contact'
      'license'           | 'license'               | 'license'
      'licenseUrl'        | 'urn:license'           | 'licenseUrl'
  }
}
