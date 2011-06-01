package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.reflect.ParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameterizedType implements MetaParameterizedType {
    ParameterizedType parameterizedType;

    public JavaReflectionParameterizedType(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public MetaType[] getTypeParameters() {
        return JavaReflectionUtil.fromTypeArray(parameterizedType.getActualTypeArguments());
    }

    public MetaType getOwnerType() {
        return JavaReflectionUtil.fromType(parameterizedType.getOwnerType());
    }

    public MetaType getRawType() {
        return JavaReflectionUtil.fromType(parameterizedType.getRawType());
    }
}
