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

package springfox.documentation.schema;

import com.fasterxml.classmate.TypeResolver;
import springfox.documentation.annotations.Incubating;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class AlternateTypeRules {
  private AlternateTypeRules() {
    throw new UnsupportedOperationException();
  }

  /**
   * Helper method to create a new alternate rule.
   *
   * @param original  the original
   * @param alternate the alternate
   * @return the alternate type rule
   */
  public static AlternateTypeRule newRule(Type original, Type alternate) {
    TypeResolver resolver = new TypeResolver();
    return new AlternateTypeRule(resolver.resolve(original), resolver.resolve(alternate));
  }

  /**
   * Helper method to create a new alternate for <code>Map&lt;K,V&gt;</code> that results in an alternate of type
   * <code>List&lt;Entry&lt;K,V&gt;&gt;</code>.
   *
   * @param key   the class that represents a key
   * @param value the value
   * @return the alternate type rule
   */
  public static AlternateTypeRule newMapRule(Class<?> key, Class<?> value) {
    TypeResolver resolver = new TypeResolver();
    return new AlternateTypeRule(resolver.resolve(Map.class, key, value),
            resolver.resolve(List.class, resolver.resolve(Entry.class, key, value)));
  }

  /**
   * Rule that transforms two dimensional arrays to list of list of array element type
   * @return the alternate type rule
   * @param resolver
   */
  @Incubating("2.3.0")
  public static AlternateTypeRule twoDimensionalArrayRule(TypeResolver resolver) {
    return new TwoDimensionalArraySubstitutionRule(resolver);
  }
}
