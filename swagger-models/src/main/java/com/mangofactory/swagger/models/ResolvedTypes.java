package com.mangofactory.swagger.models;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.types.ResolvedPrimitiveType;
import com.google.common.base.Function;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableValues;
import scala.collection.JavaConversions;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.mangofactory.swagger.models.Collections.*;
import static com.mangofactory.swagger.models.Types.*;

public class ResolvedTypes {

    public static String typeName(ResolvedType type) {
        if (isContainerType(type)) {
            return containerType(type);
        }
        return innerTypeName(type);
    }

    //DK TODO: Eliminate this repetition
    public static String responseTypeName(ResolvedType type) {
        if (isContainerType(type)) {
            return String.format("%s%s", containerType(type), optionalContainerTypeQualifierForReturn(type));
        }
        return innerTypeName(type);
    }


    private static String optionalContainerTypeQualifierForReturn(ResolvedType type) {
        if(type.isArray()) {
            return String.format("[%s]", typeName(type.getArrayElementType()));
        }

        List<ResolvedType> typeParameters = type.getTypeParameters();
        checkArgument(typeParameters.size() <= 1, "Expects container to have at most one generic parameter");
        if (typeParameters.size() == 0) {
            return "";
        }
        String qualifier = innerTypeName(typeParameters.get(0));
        if (Types.isBaseType(qualifier)) {
            return "";
        }
        return String.format("[%s]", qualifier);
    }


    private static String innerTypeName(ResolvedType type) {
        if (type.getTypeParameters().size() > 0 && type.getErasedType().getTypeParameters().length > 0) {
            return genericTypeName(type);
        }
        return simpleTypeName(type);
    }

    public static String genericTypeName(ResolvedType resolvedType) {
        StringBuilder sb = new StringBuilder(String.format("%s«", resolvedType.getErasedType().getSimpleName()));
        boolean first = true;
        for (int index = 0; index < resolvedType.getErasedType().getTypeParameters().length; index++) {
            ResolvedType typeParam = resolvedType.getTypeParameters().get(index);
            if (first) {
                sb.append(innerTypeName(typeParam));
                first = false;
            } else {
                sb.append(String.format(",%s", innerTypeName(typeParam)));
            }
        }
        sb.append("»");
        return sb.toString();
    }

    public static String simpleQualifiedTypeName(ResolvedType type) {
        if (type instanceof ResolvedPrimitiveType) {
            Type primitiveType = type.getErasedType();
            return typeNameFor(primitiveType);
        }
        return type.getErasedType().getName();
    }


    public static String simpleTypeName(ResolvedType type) {
        Class<?> erasedType = type.getErasedType();
        if (type instanceof ResolvedPrimitiveType) {
            return typeNameFor(erasedType);
        } else if (erasedType.isEnum()) {
            return "string";
        } else if (type instanceof ResolvedObjectType) {
            String typeName = typeNameFor(erasedType);
            if (typeName != null) {
                return typeName;
            }
        }
        return erasedType.getSimpleName();
    }


    public static ResolvedType asResolved(TypeResolver typeResolver, Type type) {
        if (type instanceof ResolvedType) {
            return (ResolvedType) type;
        }
        return typeResolver.resolve(type);
    }


    public static AllowableValues allowableValues(ResolvedType resolvedType) {
        if (isBaseType(simpleTypeName(resolvedType)) && resolvedType.getErasedType().isEnum()) {
            List<String> enumValues = getEnumValues(resolvedType.getErasedType());
            return new AllowableListValues(JavaConversions.collectionAsScalaIterable(enumValues).toList(), "string");
        }
        return null;
    }

    static List<String> getEnumValues(Class<?> subject) {
        return transform(Arrays.asList(subject.getEnumConstants()), new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return input.toString();
            }
        });
    }
}
