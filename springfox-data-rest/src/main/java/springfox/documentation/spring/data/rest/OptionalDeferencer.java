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
package springfox.documentation.spring.data.rest;

import org.springframework.core.convert.converter.Converter;

import java.util.Optional;

public class OptionalDeferencer<T> implements Converter<Object, T> {
  @SuppressWarnings("unchecked")
  @Override
  public T convert(Object source) {
    if (source instanceof Optional) {
      if (((Optional<T>)source).isPresent()) {
        return ((Optional<T>)source).get();
      }
    }
    return (T) source;
  }
}
