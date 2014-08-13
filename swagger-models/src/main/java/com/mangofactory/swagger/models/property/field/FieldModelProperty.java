package com.mangofactory.swagger.models.property.field;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedField;
import com.mangofactory.swagger.models.Annotations;
import com.mangofactory.swagger.models.alternates.AlternateTypeProvider;
import com.mangofactory.swagger.models.property.BaseModelProperty;

public class FieldModelProperty extends BaseModelProperty {

  private final ResolvedField childField;

  public FieldModelProperty(String name, ResolvedField childField, AlternateTypeProvider alternateTypeProvider) {
    super(name, alternateTypeProvider, Annotations.findElementAnnotation(childField.getRawMember()));
    this.childField = childField;
  }

  @Override
  protected ResolvedType realType() {
    return childField.getType();
  }
}
