package com.mangofactory.swagger.dto;

public abstract class AuthorizationType {
  protected final String type;

  protected AuthorizationType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public abstract String getName();
}
