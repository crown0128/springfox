package com.mangofactory.swagger.schema.property.field;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedField;
import com.mangofactory.swagger.schema.alternates.AlternateTypeProvider;
import com.mangofactory.swagger.schema.property.BaseModelProperty;

import static com.mangofactory.swagger.schema.Annotations.*;

public class FieldModelProperty extends BaseModelProperty {

  private final ResolvedField childField;

  public FieldModelProperty(String fieldName,
      ResolvedField childField,
      AlternateTypeProvider alternateTypeProvider) {

    super(fieldName, alternateTypeProvider, findApiModePropertyAnnotation(childField.getRawMember()));
    this.childField = childField;
  }

  @Override
  protected ResolvedType realType() {
    return childField.getType();
  }
}
