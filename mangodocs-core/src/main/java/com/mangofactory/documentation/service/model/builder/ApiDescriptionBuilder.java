package com.mangofactory.documentation.service.model.builder;

import com.google.common.collect.Ordering;
import com.mangofactory.documentation.service.model.ApiDescription;
import com.mangofactory.documentation.service.model.Operation;

import java.util.List;

public class ApiDescriptionBuilder {
  private String path;
  private String description;
  private List<Operation> operations;
  private Ordering<Operation> operationOrdering;
  private Boolean hidden;

  public ApiDescriptionBuilder(Ordering<Operation> operationOrdering) {
    this.operationOrdering = operationOrdering;
  }

  public ApiDescriptionBuilder path(String path) {
    this.path = path;
    return this;
  }

  public ApiDescriptionBuilder description(String description) {
    this.description = description;
    return this;
  }

  public ApiDescriptionBuilder operations(List<Operation> operations) {
    this.operations = operationOrdering.sortedCopy(operations);
    return this;
  }

  public ApiDescriptionBuilder hidden(Boolean hidden) {
    this.hidden = hidden;
    return this;
  }

  public ApiDescription build() {
    return new ApiDescription(path, description, operations, hidden);
  }
}