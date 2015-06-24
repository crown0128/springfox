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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.dummy.models.Pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

@Controller
@RequestMapping("/pets")
@Api(value = "", description = "Operations about pets")
public class PetService {

  @RequestMapping(value = "/{petId}", method = RequestMethod.GET)
  @ApiOperation(value = "Find pet by ID", notes = "Returns a pet when ID < 10. "
      + "ID > 10 or nonintegers will simulate API error conditions",
      response = Pet.class
  )
  public Pet getPetById(
      @ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]",
          required = true) @PathVariable("petId") String petId) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(method = RequestMethod.POST)
  @ApiOperation(value = "Add a new pet to the store")
  @ApiResponses(value = { @ApiResponse(code = 405, message = "Invalid input") })
  public void addPet(
      @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(method = RequestMethod.PUT)
  @ApiOperation(value = "Update an existing pet")
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
      @ApiResponse(code = 404, message = "Pet not found"),
      @ApiResponse(code = 405, message = "Validation exception") })
  public void updatePet(
      @ApiParam(value = "Pet object that needs to be added to the store", required = true) @RequestBody Pet pet) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/findByStatus", method = RequestMethod.GET, params = {"status"})
  @ApiOperation(value = "Finds Pets by status",
      notes = "Multiple status values can be provided with comma seperated strings",
      response = Pet.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid status value") })
  public Pet findPetsByStatus(
      @ApiParam(value = "Status values that need to be considered for filter", required = true,
          defaultValue = "available", allowableValues = "available,pending,sold", allowMultiple = true)
      @RequestParam("status") String status) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/findByTags", method = RequestMethod.GET)
  @ApiOperation(value = "Finds Pets by tags",
      notes = "Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.",
      response = Pet.class)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid tag value") })
  @Deprecated
  public Pet findPetsByTags(
      @ApiParam(value = "Tags to filter by", required = true, allowMultiple = true)
      @RequestParam("tags") String tags) {
    throw new RuntimeException("NotImplementedException");
  }


  @RequestMapping(value = "/siblings", method = RequestMethod.POST)
  public List<Pet> siblings(Pet pet) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(method = RequestMethod.GET)
  @ApiOperation(value = "List all pets")
  //@ApiModel(type = Pet.class, collection = true)
  public
  @ResponseBody
  List<Pet> listPets() {
    return newArrayList();
  }

  @RequestMapping(value = "/{name}", method = RequestMethod.POST)
  public HttpEntity<Pet> petByName(@PathVariable String name) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/echo", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Pet> echo(@RequestBody Map<String, Pet> someInput) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/transformPetNameToPetMapToAny", method = RequestMethod.POST)
  public Map<String, Object> transformPetNameToPetMapToAny(Map<String, Pet> someInput) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/transformPetNameToPetMapToGenericOpenMap", method = RequestMethod.POST)
  public Map<Object, Object> transformPetNameToPetMapToGenericOpenMap(Map<String, Pet> someInput) {
    throw new RuntimeException("NotImplementedException");
  }


  @RequestMapping(value = "/transformPetNameToPetMapToOpenMap", method = RequestMethod.POST)
  public Map transformPetNameToPetMapToOpenMap(Map<String, Pet> someInput) {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "/nameToNickNamesMap", method = RequestMethod.POST)
  public Map<String, List<String>> nameToNickNamesMap() {
    throw new RuntimeException("NotImplementedException");
  }

  @RequestMapping(value = "byName/{name}", method = RequestMethod.POST)
  public HttpEntity<List<Pet>> petEntities(@PathVariable String name) {
    return new ResponseEntity<List<Pet>>(new ArrayList<Pet>(), HttpStatus.OK);
  }

  @RequestMapping(value = "{a}/{b}", method = RequestMethod.GET)
  public ResponseEntity<Void> method(@PathVariable("a") String a, @PathVariable("b") String b) {
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @RequestMapping(value = "{petId}/pic", method = RequestMethod.POST)
  public ResponseEntity<Void> updatePic(@PathVariable String petId, MultipartFile pic) {
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @RequestMapping(value = "{petId}/pic/{picId}", method = RequestMethod.GET)
  public ResponseEntity<Void> updatePic(@PathVariable String petId, @PathVariable String picId) {
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

}
