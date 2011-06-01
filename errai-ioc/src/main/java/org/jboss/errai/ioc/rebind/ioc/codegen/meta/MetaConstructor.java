package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

public abstract class MetaConstructor implements MetaClassMember, MetaGenericDeclaration {
    public abstract MetaParameter[] getParameters();

    public abstract MetaType[] getGenericParameterTypes();

    public abstract boolean isVarArgs();
}
