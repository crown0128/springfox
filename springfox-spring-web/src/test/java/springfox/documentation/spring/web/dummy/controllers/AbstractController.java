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

package springfox.documentation.spring.web.dummy.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public abstract class AbstractController<T> {

  @RequestMapping(value = "/create-t", method = RequestMethod.PUT)
  public void create(T toCreate) {
    throw new UnsupportedOperationException();
  }

  @RequestMapping(value = "/get-t", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
  public T get() {
    throw new UnsupportedOperationException();
  }
}
