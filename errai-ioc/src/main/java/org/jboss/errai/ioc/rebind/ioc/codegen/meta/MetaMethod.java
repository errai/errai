package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

public abstract class MetaMethod implements MetaClassMember, MetaGenericDeclaration {
    public abstract String getName();

    public abstract MetaClass getReturnType();

    public abstract MetaType getGenericReturnType();

    public abstract MetaType[] getGenericParameterTypes();

    public abstract MetaParameter[] getParameters();
}
