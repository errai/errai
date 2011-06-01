package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericArrayType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.reflect.GenericArrayType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionGenericArrayType implements MetaGenericArrayType {
    private GenericArrayType type;

    public JavaReflectionGenericArrayType(GenericArrayType type) {
        this.type = type;
    }

    public MetaType getGenericComponentType() {
        return JavaReflectionUtil.fromType(type.getGenericComponentType());
    }
}
