package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionUtil {

    public static MetaTypeVariable[] fromTypeVariable(TypeVariable[] typeVariables) {
        List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

        for (TypeVariable typeVariable : typeVariables) {
            typeVariableList.add(new JavaReflectionTypeVariable(typeVariable));
        }

        return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
    }

    public static MetaType[] fromTypeArray(Type[] types) {
        List<MetaType> typeList = new ArrayList<MetaType>();

        for (Type t : types) {
            typeList.add(fromType(t));
        }

        return typeList.toArray(new MetaType[types.length]);
    }

    public static MetaType fromType(Type t) {
        if (t instanceof Class) {
            return (MetaClassFactory.get((Class) t));
        } else if (t instanceof TypeVariable) {
            return new JavaReflectionTypeVariable((TypeVariable) t);
        } else if (t instanceof ParameterizedType) {
            return new JavaReflectionParameterizedType((ParameterizedType) t);
        } else if (t instanceof GenericArrayType) {
            return new JavaReflectionGenericArrayType((GenericArrayType) t);
        } else if (t instanceof GenericDeclaration) {
            return new JavaReflectionGenericDeclaration((GenericDeclaration) t);
        } else if (t instanceof WildcardType) {
            return new JavaReflectionWildcardType((WildcardType) t);
        } else {
            return null;
        }
    }
}
