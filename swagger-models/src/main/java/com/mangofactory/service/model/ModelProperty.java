package com.mangofactory.service.model;

import com.fasterxml.classmate.ResolvedType;

public class ModelProperty {
  private final String name;
  private final ResolvedType type;
  private final String qualifiedType;
  private final int position;
  private final Boolean required;
  private final String description;
  private final AllowableValues allowableValues;

  private final ModelRef items;

  public ModelProperty(String name, ResolvedType type, String qualifiedType, int position, Boolean required, String description,
                       AllowableValues allowableValues, ModelRef items) {
    this.name = name;
    this.type = type;
    this.qualifiedType = qualifiedType;
    this.position = position; //TODO Suspect unused
    this.required = required;
    this.description = description;
    this.allowableValues = allowableValues;
    this.items = items;
  }

  public String getName() {
    return name;
  }

  public ResolvedType getType() {
    return type;
  }

  public String getQualifiedType() {
    return qualifiedType;
  }

  public int getPosition() {
    return position;
  }

  public Boolean isRequired() {
    return required;
  }

  public String getDescription() {
    return description;
  }

  public AllowableValues getAllowableValues() {
    return allowableValues;
  }

  public ModelRef getItems() {
    return items;
  }
}
