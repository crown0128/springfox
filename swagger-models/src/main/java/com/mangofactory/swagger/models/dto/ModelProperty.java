package com.mangofactory.swagger.models.dto;

public class ModelProperty {
  private final String type;
  private final String qualifiedType;
  private final int position;
  private final Boolean required;
  private final String description;
  private final AllowableValues allowableValues;
  private final ModelRef items;

  public ModelProperty(String type, String qualifiedType, int position, Boolean required, String description,
                       AllowableValues allowableValues, ModelRef items) {
    this.type = type;
    this.qualifiedType = qualifiedType;
    this.position = position;
    this.required = required;
    this.description = description;
    this.allowableValues = allowableValues;
    this.items = items;
  }

  public String getType() {
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
