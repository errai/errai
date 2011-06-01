package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaWildcardType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.reflect.WildcardType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionWildcardType implements MetaWildcardType {
    private WildcardType wildcardType;

    public JavaReflectionWildcardType(WildcardType wildcardType) {
        this.wildcardType = wildcardType;
    }

    public MetaType[] getLowerBounds() {
        return JavaReflectionUtil.fromTypeArray(wildcardType.getLowerBounds());
    }

    public MetaType[] getUpperBounds() {
        return JavaReflectionUtil.fromTypeArray(wildcardType.getUpperBounds());
    }
}
