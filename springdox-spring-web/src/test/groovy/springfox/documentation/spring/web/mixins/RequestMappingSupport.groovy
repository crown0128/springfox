package springfox.documentation.spring.web.mixins

import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import springfox.documentation.spring.web.dummy.controllers.PetGroomingService
import springfox.documentation.spring.web.dummy.DummyClass
import springfox.documentation.spring.web.dummy.DummyController
import springfox.documentation.spring.web.dummy.DummyControllerWithApiDescription
import springfox.documentation.spring.web.dummy.controllers.FancyPetService
import springfox.documentation.spring.web.dummy.controllers.PetService
import springfox.documentation.spring.web.dummy.models.FancyPet

import javax.servlet.ServletContext

class RequestMappingSupport {

  RequestMappingInfo requestMappingInfo(String path, Map overrides = [:]) {
    PatternsRequestCondition singlePatternRequestCondition = patternsRequestCondition([path] as String[])
    ConsumesRequestCondition consumesRequestCondition = overrides['consumesRequestCondition'] ?: consumesRequestCondition()
    ProducesRequestCondition producesRequestCondition = overrides['producesRequestCondition'] ?: producesRequestCondition()
    PatternsRequestCondition patternsRequestCondition = overrides['patternsRequestCondition'] ?: singlePatternRequestCondition
    ParamsRequestCondition paramsRequestCondition = overrides["paramsCondition"] ?: paramsRequestCondition()
    RequestMethodsRequestCondition requestMethodsRequestCondition =
            overrides['requestMethodsRequestCondition'] ?: requestMethodsRequestCondition(RequestMethod.values())

    new RequestMappingInfo(patternsRequestCondition, requestMethodsRequestCondition, paramsRequestCondition, null, consumesRequestCondition, producesRequestCondition, null)
  }

  HandlerMethod dummyHandlerMethod(String methodName = "dummyMethod", Class<?>... parameterTypes = null) {
    def clazz = new DummyClass()
    Class c = clazz.getClass();
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }

  HandlerMethod handlerMethodIn(Class<?> aClass, String methodName = "dummyMethod", Class<?>... parameterTypes = null) {
    new HandlerMethod(aClass, aClass.getMethod(methodName, parameterTypes))
  }

  HandlerMethod dummyControllerHandlerMethod(String methodName = "dummyMethod", parameterTypes = null) {
    def clazz = new DummyController()
    Class c = clazz.getClass();
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }

  HandlerMethod dummyControllerWithApiDescriptionHandlerMethod(String methodName = "dummyMethod",
      parameterTypes = null) {

    def clazz = new DummyControllerWithApiDescription()
    Class c = clazz.getClass();
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }


  HandlerMethod petServiceHandlerMethod(String methodName = "getPetById", parameterTypes = String) {
    def clazz = new PetService()
    Class c = clazz.getClass()
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }

  HandlerMethod fancyPetServiceHandlerMethod(String methodName = "createObject", parameterTypes = FancyPet) {
    def clazz = new FancyPetService()
    Class c = clazz.getClass()
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }


  HandlerMethod multipleRequestMappingsHandlerMethod(String methodName = "canGroom", parameterTypes = String) {
    def clazz = new PetGroomingService()
    Class c = clazz.getClass()
    new HandlerMethod(clazz, c.getMethod(methodName, parameterTypes))
  }

  Class ignorableClass() {
    DummyClass.ApiIgnorableClass
  }

  def apiImplicitParamsClass() {
    DummyClass.ApiImplicitParamsClass.class;
  }

  HandlerMethod ignorableHandlerMethod() {
    def clazz = new DummyClass.ApiIgnorableClass()
    Class c = clazz.getClass();
    new HandlerMethod(clazz, c.getMethod("dummyMethod", null))
  }

  PatternsRequestCondition patternsRequestCondition(String... patterns) {
    new PatternsRequestCondition(patterns)
  }

  ParamsRequestCondition paramsRequestCondition(String... params) {
    new ParamsRequestCondition(params)
  }

  ConsumesRequestCondition consumesRequestCondition(String... conditions) {
    new ConsumesRequestCondition(conditions)
  }

  ProducesRequestCondition producesRequestCondition(String... conditions) {
    new ProducesRequestCondition(conditions)
  }

  RequestMethodsRequestCondition requestMethodsRequestCondition(RequestMethod... requestMethods) {
    new RequestMethodsRequestCondition(requestMethods)
  }

  ServletContext servletContext() {
    [getContextPath: { return "/context-path" }] as ServletContext
  }
}
