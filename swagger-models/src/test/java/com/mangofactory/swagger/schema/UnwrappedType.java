package com.mangofactory.swagger.schema;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class UnwrappedType {
  private Category category;

  @JsonUnwrapped
  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }
}
