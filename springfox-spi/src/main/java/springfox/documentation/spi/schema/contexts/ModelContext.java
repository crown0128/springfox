/*
 *
 *  Copyright 2015-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.documentation.spi.schema.contexts;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import springfox.documentation.builders.ModelBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.AlternateTypeProvider;
import springfox.documentation.spi.schema.GenericTypeNamingStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class ModelContext {
  private final String parameterId;
  private final ResolvedType type;
  private final boolean returnType;
  private final String groupName;
  private final DocumentationType documentationType;

  private final Optional<ResolvedType> view;
  private final Set<ResolvedType> validationGroups;

  private final ModelContext parentContext;
  private final Set<ResolvedType> seenTypes = new HashSet<>();
  private final ModelBuilder modelBuilder;
  private final AlternateTypeProvider alternateTypeProvider;
  private final GenericTypeNamingStrategy genericNamingStrategy;
  private final Set<Class> ignorableTypes;
  private final Map<ResolvedType, String> registeredTypes;

  @SuppressWarnings("ParameterNumber")
  private ModelContext(
      String parameterId,
      String groupName,
      ResolvedType type,
      boolean returnType,
      Optional<ResolvedType> view,
      Set<ResolvedType> validationGroups,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeProvider,
      GenericTypeNamingStrategy genericNamingStrategy,
      Set<Class> ignorableTypes) {
    this.parameterId = parameterId;
    this.groupName = groupName;
    this.documentationType = documentationType;
    this.alternateTypeProvider = alternateTypeProvider;
    this.genericNamingStrategy = genericNamingStrategy;
    this.ignorableTypes = ignorableTypes;
    this.registeredTypes = new HashMap<>();
    this.parentContext = null;
    this.type = type;
    this.returnType = returnType;
    this.view = view;
    this.validationGroups = new HashSet<>(validationGroups);
    this.modelBuilder =
        new ModelBuilder(String.format(
            "%s_%s",
            parameterId,
            type.getBriefDescription()));
  }

  @SuppressWarnings("ParameterNumber")
  private ModelContext(
      ModelContext parentContext,
      ResolvedType input) {
    this.parameterId = parentContext.parameterId;
    this.parentContext = parentContext;
    this.type = input;
    this.groupName = parentContext.groupName;
    this.returnType = parentContext.isReturnType();
    this.view = parentContext.getView();
    this.validationGroups = parentContext.getValidationGroups();
    this.documentationType = parentContext.getDocumentationType();
    this.alternateTypeProvider = parentContext.alternateTypeProvider;
    this.ignorableTypes = parentContext.ignorableTypes;
    this.registeredTypes = parentContext.registeredTypes;
    this.genericNamingStrategy = parentContext.getGenericNamingStrategy();
    this.modelBuilder =
        new ModelBuilder(String.format(
            "%s_%s",
            parameterId,
            input.getBriefDescription()));
  }

  /**
   * @return type behind this context
   */
  public ResolvedType getType() {
    return type;
  }

  /**
   * @return parameter id behind this context
   */
  public String getParameterId() {
    return parameterId;
  }

  /**
   * @return type id behind this context
   */
  public String getTypeId() {
    return String.format(
        "%s_%s",
        parameterId,
        type.getBriefDescription());
  }

  /**
   * @param resolver - type resolved
   * @return resolved type
   */
  public ResolvedType resolvedType(TypeResolver resolver) {
    return resolver.resolve(getType());
  }

  /**
   * @return is the context for a return type
   */
  public boolean isReturnType() {
    return returnType;
  }

  /**
   * @return view
   */
  public Optional<ResolvedType> getView() {
    return view;
  }

  /**
   * @return a set of jsr-303 validation groups
   */
  public Set<ResolvedType> getValidationGroups() {
    return validationGroups;
  }

  /**
   * @return alternate type provider that's available to this context
   */
  public AlternateTypeProvider getAlternateTypeProvider() {
    return alternateTypeProvider;
  }

  /**
   * @param resolved - type to find an alternate type for
   * @return alternate type for given resolved type
   */
  public ResolvedType alternateFor(ResolvedType resolved) {
    return alternateTypeProvider.alternateFor(resolved);
  }

  /**
   * @return group name of the docket
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Convenience method to provide an new context for an input parameter
   *
   * @param group                 - group name of the docket
   * @param type                  - type
   * @param documentationType     - for documentation type
   * @param alternateTypeProvider - alternate type provider
   * @param genericNamingStrategy - how generic types should be named
   * @param ignorableTypes        - types that can be ignored
   * @return new context
   */
  @SuppressWarnings("ParameterNumber")
  public static ModelContext inputParam(
      String parameterId,
      String group,
      ResolvedType type,
      Optional<ResolvedType> view,
      Set<ResolvedType> validationGroups,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeProvider,
      GenericTypeNamingStrategy genericNamingStrategy,
      Set<Class> ignorableTypes) {

    return new ModelContext(
        parameterId,
        group,
        type,
        false,
        view,
        validationGroups,
        documentationType,
        alternateTypeProvider,
        genericNamingStrategy,
        ignorableTypes);
  }

  /**
   * Convenience method to provide an new context for an return parameter
   *
   * @param groupName             - group name of the docket
   * @param type                  - type
   * @param documentationType     - for documentation type
   * @param alternateTypeProvider - alternate type provider
   * @param genericNamingStrategy - how generic types should be named
   * @param ignorableTypes        - types that can be ignored
   * @return new context
   */
  @SuppressWarnings("ParameterNumber")
  public static ModelContext returnValue(
      String parameterId,
      String groupName,
      ResolvedType type,
      Optional<ResolvedType> view,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeProvider,
      GenericTypeNamingStrategy genericNamingStrategy,
      Set<Class> ignorableTypes) {

    return new ModelContext(
        parameterId,
        groupName,
        type,
        true,
        view,
        new HashSet<>(),
        documentationType,
        alternateTypeProvider,
        genericNamingStrategy,
        ignorableTypes);
  }

  /**
   * Convenience method to provide an new context for an input parameter
   *
   * @param context - parent context
   * @param input   - context for given input
   * @return new context based on parent context for a given input
   */
  public static ModelContext fromParent(
      ModelContext context,
      ResolvedType input) {
    return new ModelContext(
        context,
        input);
  }

  /**
   * Answers the question, has the given type been processed?
   *
   * @param resolvedType - type to check
   * @return true or false
   */
  public boolean hasSeenBefore(ResolvedType resolvedType) {
    return seenTypes.contains(resolvedType)
        || seenTypes.contains(new TypeResolver().resolve(resolvedType.getErasedType()))
        || parentHasSeenBefore(resolvedType);
  }

  public DocumentationType getDocumentationType() {
    return documentationType;
  }

  /**
   * Answers the question, has the given type been processed by its parent context?
   *
   * @param resolvedType - type to check
   * @return true or false
   */
  private boolean parentHasSeenBefore(ResolvedType resolvedType) {
    if (parentContext == null) {
      return false;
    }
    return parentContext.hasSeenBefore(resolvedType);
  }

  public GenericTypeNamingStrategy getGenericNamingStrategy() {
    if (parentContext == null) {
      return genericNamingStrategy;
    }
    return parentContext.getGenericNamingStrategy();
  }

  public ModelBuilder getBuilder() {
    return modelBuilder;
  }

  public void seen(ResolvedType resolvedType) {
    seenTypes.add(resolvedType);
  }

  @Override
  @SuppressWarnings("CyclomaticComplexity")
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModelContext that = (ModelContext) o;

    return
        Objects.equals(parameterId, that.parameterId)
            && Objects.equals(groupName, that.groupName)
            && Objects.equals(type, that.type)
            && Objects.equals(view, that.view)
            && Objects.equals(validationGroups, that.validationGroups)
            && Objects.equals(documentationType, that.documentationType)
            && Objects.equals(returnType, that.returnType)
            && Objects.equals(namingStrategy(), that.namingStrategy());
  }

  private String namingStrategy() {
    if (genericNamingStrategy != null) {
      return genericNamingStrategy.getClass().getName();
    }
    return "";
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        parameterId,
        groupName,
        type,
        view,
        validationGroups,
        documentationType,
        returnType,
        namingStrategy());
  }

  public String description() {
    return new StringBuilder(this.getClass().getSimpleName())
        .append("{")
        .append("groupName=").append(this.getGroupName()).append(", ")
        .append("type=").append(this.getType()).append(", ")
        .append("isReturnType=").append(this.isReturnType())
        .append("view=").append(this.getView())
        .append("}").toString();
  }

  public boolean canIgnore(ResolvedType type) {
    return ignorableTypes.contains(type.getErasedType());
  }
}
