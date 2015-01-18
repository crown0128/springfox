package com.mangofactory.schema

import com.mangofactory.swagger.mixins.ModelProviderSupport
import com.mangofactory.swagger.mixins.TypesForTestingSupport
import com.mangofactory.service.model.Model
import spock.lang.Specification

import static com.google.common.collect.Lists.*
import static com.mangofactory.schema.plugins.ModelContext.*

@Mixin([TypesForTestingSupport, ModelProviderSupport])
class EnumTypeSpec extends Specification {
  def "enum type are inferred as type string with allowable values" () {
    given:
      def list = newArrayList("ONE", "TWO")
      def provider = defaultModelProvider()
      Model asInput = provider.modelFor(inputParam(enumType(), documentationType())).get()
      Model asReturn = provider.modelFor(returnValue(enumType(), documentationType())).get()

    expect:
      asInput.getName() == "ExampleWithEnums"
      asInput.getProperties().containsKey("exampleEnum")
      def modelPropertyOption = asInput.getProperties().get("exampleEnum")
      def modelProperty = modelPropertyOption


      modelProperty.type.erasedType == ExampleEnum
      modelProperty.getQualifiedType() == "com.mangofactory.schema.ExampleEnum"
      modelProperty.getItems() == null
      modelProperty.getAllowableValues().getValues() == list

      asReturn.getName() == "ExampleWithEnums"
      asReturn.getProperties().containsKey("exampleEnum")
      def retModelPropertyOption = asReturn.getProperties().get("exampleEnum")
      def retModelProperty = retModelPropertyOption
      retModelProperty.type.erasedType == ExampleEnum
      retModelProperty.getQualifiedType() == "com.mangofactory.schema.ExampleEnum"
      retModelProperty.getItems() == null
      retModelProperty.getAllowableValues().getValues() == list
  }
}
