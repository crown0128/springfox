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

package springfox.documentation.schema


import spock.lang.Specification
import spock.lang.Unroll
import springfox.documentation.service.AllowableRangeValues
import springfox.documentation.service.AllowableValues

class ModelRefSpec extends Specification {
  @Unroll
  def "map types are reflected correctly"() {
    expect:
    model.isCollection() == isCollection
    model.isMap() == isMap

    where:
    model                                               | isCollection | isMap
    new ModelRef("string")                              | false        | false
    new ModelRef("string", null as ModelReference)      | false        | false
    new ModelRef("string", null, true)                  | false        | false
    new ModelRef("string", new ModelRef("List"), true)  | false        | true
    new ModelRef("string", new ModelRef("List"), false) | true         | false
    new ModelRef("string", new ModelRef("Map"), true)   | false        | true
    new ModelRef("string", new ModelRef("Map"), false)  | true         | false
    new ModelRef("string", Mock(AllowableValues))       | false        | false
  }

  def ".equals and .hashCode works as expected"() {
    given:
    ModelReference model = new ModelRef("string")

    expect:
    model.equals(testModel) == expectedEquality
    testModel.equals(model) == expectedEquality
    model.equals(model)
    !model.equals(null)
    !model.equals(new Object())

    and:
    (model.hashCode() == testModel.hashCode()) == expectedEquality
    model.hashCode() == model.hashCode()

    where:
    testModel                                                                     | expectedEquality
    new ModelRef("string")                                                        | true
    new ModelRef("string", null as ModelReference)                                | true
    new ModelRef("integer", null, true)                                           | false
    new ModelRef("string", null, true)                                            | false
    new ModelRef("string", new AllowableRangeValues("3", "5"))                    | false
    new ModelRef("string", new ModelRef("List"), false)                           | false
    new ModelRef("string", "java.lang.String", new ModelRef("Map"), null, "3434") | false
    new ModelRef("string", "java.lang.String", null, null, "3434")                | false
    new ModelRef("string", "java.lang.String", null, null, "3434")                | false
    new ModelRef("string", null, null, null, "3434")                              | false
    new ModelRef("string", "java.lang.String", null, null, null)                  | false
  }
}
