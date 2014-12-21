package com.mangofactory.swagger.models;

import com.mangofactory.servicemodel.Model;

public interface ModelProvider {
  com.google.common.base.Optional<Model> modelFor(ModelContext modelContext);

  java.util.Map<String, Model> dependencies(ModelContext modelContext);
}
