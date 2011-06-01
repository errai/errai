package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.annotation.Annotation;

public abstract class MetaField implements HasAnnotations, MetaClassMember {
    public abstract MetaClass getType();

    public abstract MetaType getGenericType();

    public abstract String getName();

    public abstract Annotation[] getAnnotations();
}
