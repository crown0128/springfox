package com.mangofactory.documentation.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.mangofactory.documentation.schema.property.provider.ModelPropertiesProvider;
import com.mangofactory.documentation.spi.schema.contexts.ModelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.FluentIterable.*;
import static com.google.common.collect.Lists.*;
import static com.mangofactory.documentation.schema.Collections.*;
import static com.mangofactory.documentation.schema.Maps.*;
import static com.mangofactory.documentation.spi.schema.contexts.ModelContext.*;

@Component
public class ModelDependencyProvider {

  private final TypeResolver typeResolver;
  private final ModelPropertiesProvider propertiesProvider;
  private final TypeNameExtractor nameExtractor;

  @Autowired
  public ModelDependencyProvider(TypeResolver typeResolver,
                                 @Qualifier("default") ModelPropertiesProvider propertiesProvider,
                                 TypeNameExtractor nameExtractor) {
    this.typeResolver = typeResolver;
    this.propertiesProvider = propertiesProvider;
    this.nameExtractor = nameExtractor;
  }

  public Set<ResolvedType> dependentModels(ModelContext modelContext) {
    return
            from(resolvedDependencies(modelContext))
            .filter(ignorableTypes(modelContext))
            .filter(not(baseTypes(modelContext)))
            .toSet();
  }

  private Predicate<ResolvedType> baseTypes(final ModelContext modelContext) {
    return new Predicate<ResolvedType>() {
      @Override
      public boolean apply(ResolvedType resolvedType) {
        return isBaseType(fromParent(modelContext, resolvedType));
      }
    };
  }

  private boolean isBaseType(ModelContext modelContext) {
    String typeName = nameExtractor.typeName(modelContext);
    return Types.isBaseType(typeName);
  }

  private Predicate<ResolvedType> ignorableTypes(final ModelContext modelContext) {
    return new Predicate<ResolvedType>() {
      @Override
      public boolean apply(ResolvedType input) {
        return !modelContext.hasSeenBefore(input);
      }
    };
  }


  private List<ResolvedType> resolvedDependencies(ModelContext modelContext) {
    ResolvedType resolvedType = modelContext.alternateFor(modelContext.resolvedType(typeResolver));
    if (isBaseType(fromParent(modelContext, resolvedType))) {
      modelContext.seen(resolvedType);
      return newArrayList();
    }
    List<ResolvedType> dependencies = newArrayList(resolvedTypeParameters(modelContext, resolvedType));
    dependencies.addAll(resolvedPropertiesAndFields(modelContext, resolvedType));
    return dependencies;
  }

  private List<? extends ResolvedType> resolvedTypeParameters(ModelContext modelContext, ResolvedType resolvedType) {
    List<ResolvedType> parameters = newArrayList();
    for (ResolvedType parameter : resolvedType.getTypeParameters()) {
      parameters.add(modelContext.alternateFor(parameter));
      parameters.addAll(resolvedDependencies(fromParent(modelContext, parameter)));
    }
    return parameters;
  }

  private List<ResolvedType> resolvedPropertiesAndFields(ModelContext modelContext, ResolvedType resolvedType) {
    if (modelContext.hasSeenBefore(resolvedType) || resolvedType.getErasedType().isEnum()) {
      return newArrayList();
    }
    modelContext.seen(resolvedType);
    List<ResolvedType> properties = newArrayList();
    for (ModelProperty property : nonTrivialProperties(modelContext, resolvedType)) {
      properties.add(property.getType());
      properties.addAll(maybeFromCollectionElementType(modelContext, property));
      properties.addAll(maybeFromMapValueType(modelContext, property));
      properties.addAll(maybeFromRegularType(modelContext, property));
    }
    return properties;
  }

  private FluentIterable<ModelProperty> nonTrivialProperties(ModelContext modelContext, ResolvedType resolvedType) {
    return from(propertiesFor(modelContext, resolvedType))
            .filter(not(baseProperty(modelContext)));
  }

  private Predicate<? super ModelProperty> baseProperty(final ModelContext modelContext) {
    return new Predicate<ModelProperty>() {
      @Override
      public boolean apply(ModelProperty input) {
        return isBaseType(fromParent(modelContext, input.getType()));
      }
    };
  }

  private List<ResolvedType> maybeFromRegularType(ModelContext modelContext, ModelProperty property) {
    if (isContainerType(property.getType()) || isMapType(property.getType())) {
      return newArrayList();
    }
    return newArrayList(resolvedDependencies(fromParent(modelContext, property.getType())));
  }

  private List<ResolvedType> maybeFromCollectionElementType(ModelContext modelContext, ModelProperty property) {
    List<ResolvedType> dependencies = newArrayList();
    if (isContainerType(property.getType())) {
      ResolvedType collectionElementType = collectionElementType(property.getType());
      if (!isBaseType(fromParent(modelContext, collectionElementType))) {
        dependencies.add(collectionElementType);
      }
      dependencies.addAll(resolvedDependencies(fromParent(modelContext, collectionElementType)));
    }
    return dependencies;
  }

  private List<ResolvedType> maybeFromMapValueType(ModelContext modelContext, ModelProperty property) {
    List<ResolvedType> dependencies = newArrayList();
    if (isMapType(property.getType())) {
      ResolvedType valueType = mapValueType(property.getType());
      if (!isBaseType(fromParent(modelContext, valueType))) {
        dependencies.add(valueType);
      }
      dependencies.addAll(resolvedDependencies(fromParent(modelContext, valueType)));
    }
    return dependencies;
  }

  private List<ModelProperty> propertiesFor(ModelContext modelContext, ResolvedType resolvedType) {
    return propertiesProvider.propertiesFor(resolvedType, modelContext);
  }


}
