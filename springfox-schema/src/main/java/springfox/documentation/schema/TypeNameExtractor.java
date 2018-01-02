/*
 *
 *  Copyright 2015 the original author or authors.
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

package springfox.documentation.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.ResolvedArrayType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.types.ResolvedPrimitiveType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.schema.GenericTypeNamingStrategy;
import springfox.documentation.spi.schema.TypeNameProviderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Optional.fromNullable;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Collections.containerType;
import static springfox.documentation.schema.Types.typeNameFor;
import static springfox.documentation.schema.Maps.isMapType;

@Component
public class TypeNameExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(TypeNameExtractor.class);
  
  private final TypeResolver typeResolver;
  private final PluginRegistry<TypeNameProviderPlugin, DocumentationType> typeNameProviders;
  private final EnumTypeDeterminer enumTypeDeterminer;

  @Autowired
  public TypeNameExtractor(
      TypeResolver typeResolver,
      @Qualifier("typeNameProviderPluginRegistry")
      PluginRegistry<TypeNameProviderPlugin, DocumentationType> typeNameProviders,
      EnumTypeDeterminer enumTypeDeterminer) {

    this.typeResolver = typeResolver;
    this.typeNameProviders = typeNameProviders;
    this.enumTypeDeterminer = enumTypeDeterminer;
  }

  public String typeName(ModelContext context) {
    ResolvedType type = asResolved(context.getType());
    if (isContainerType(type)) {
      return containerType(type);
    }
    return innerTypeName(type, context);
  }

  private ResolvedType asResolved(Type type) {
    return typeResolver.resolve(type);
  }

  private String genericTypeName(ResolvedType resolvedType, ModelContext context) {
    Class<?> erasedType = resolvedType.getErasedType();
    GenericTypeNamingStrategy namingStrategy = context.getGenericNamingStrategy();
    String simpleName = fromNullable(isContainerType(resolvedType)?
        containerType(resolvedType):typeNameFor(erasedType))
            .or(modelName(ModelContext.fromParent(context, resolvedType)));
    StringBuilder sb = new StringBuilder(String.format("%s%s", simpleName, namingStrategy.getOpenGeneric()));
    boolean first = true;
    for (int index = 0; index < erasedType.getTypeParameters().length; index++) {
      ResolvedType typeParam = resolvedType.getTypeParameters().get(index);
      if (first) {
        sb.append(innerTypeName(typeParam, context));
        first = false;
      } else {
        sb.append(String.format("%s%s", namingStrategy.getTypeListDelimiter(),
            innerTypeName(typeParam, context)));
      }
    }
    sb.append(namingStrategy.getCloseGeneric());
    return sb.toString();
  }

  private String innerTypeName(ResolvedType type, ModelContext context) {
    if (type.getTypeParameters().size() > 0 && type.getErasedType().getTypeParameters().length > 0) {
      return genericTypeName(type, context);
    }
    return simpleTypeName(type, context);
  }

  private String simpleTypeName(ResolvedType type, ModelContext context) {
    Class<?> erasedType = type.getErasedType();
    if (type instanceof ResolvedPrimitiveType) {
      return typeNameFor(erasedType);
    } else if (enumTypeDeterminer.isEnum(erasedType)) {
      return "string";
    } else if (type instanceof ResolvedArrayType) {
      GenericTypeNamingStrategy namingStrategy = context.getGenericNamingStrategy();
      return String.format("Array%s%s%s", namingStrategy.getOpenGeneric(),
          simpleTypeName(type.getArrayElementType(), context), namingStrategy.getCloseGeneric());
    } else if (type instanceof ResolvedObjectType) {
      String typeName = typeNameFor(erasedType);
      if (typeName != null) {
        return typeName;
      }
    }
    return modelName(ModelContext.fromParent(context, type));
  }

  private String modelName(ModelContext context) {
    if (context.isFullTypeNameRequired() &&
        context.getTypeName().isPresent() &&
        !isMapType(asResolved(context.getType()))) {
      return adjustedName(context);
    }
    TypeNameProviderPlugin selected =
        typeNameProviders.getPluginFor(context.getDocumentationType(), new DefaultTypeNameProvider());
    String modelName = selected.nameFor(((ResolvedType)context.getType()).getErasedType());
    LOG.debug("Generated unique model named: {}, with model id: {}", modelName, context.hashCode());
    context.registerTypeName(modelName);
    return modelName;
  }

  private String adjustedName(ModelContext context) {
    LOG.debug("Building models indexes for type {}", context.getTypeName().get());
    Set<Integer> modelIds = new TreeSet<Integer>(context.getSimilarTypes());
    Map<Integer, Integer> links = context.getTypeEquality();
    modelIds.removeAll(links.keySet());
    String rawTypeName = context.getTypeName().get();
    if (modelIds.size() == 1) {
      return rawTypeName;
    }

    Integer currentModelId = context.hashCode();
    int i = 1;

    while (links.containsKey(currentModelId)) {
      Integer nextModelId = links.get(currentModelId);
      if (nextModelId.equals(currentModelId)) {
        break;
      }
      currentModelId = nextModelId;
    }

    for(Integer modelId: modelIds) {
      if (modelId.equals(currentModelId)) {
        return String.format("%s_%s", rawTypeName, i);
      }
      ++i;
    }
    return rawTypeName;
  }
}
