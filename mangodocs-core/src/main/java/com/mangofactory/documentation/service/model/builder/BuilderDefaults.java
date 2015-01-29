package com.mangofactory.documentation.service.model.builder;

import com.google.common.base.Optional;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class BuilderDefaults {
  private BuilderDefaults() {
    throw new UnsupportedOperationException();
  }

  public static <T> T defaultIfAbsent(T newValue, T defaultValue) {
      return Optional.fromNullable(newValue)
            .or(Optional.fromNullable(defaultValue))
            .orNull();
  }

  public static <T> List<T> nullToEmptyList(List<T> newValue) {
    if (newValue == null) {
      return newArrayList();
    }
    return newValue;
  }
}
