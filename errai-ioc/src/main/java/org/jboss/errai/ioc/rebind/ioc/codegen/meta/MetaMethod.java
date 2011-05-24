package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

public interface MetaMethod extends MetaClassMember {
    public String getName();

    public MetaClass getReturnType();

    public MetaParameter[] getParameters();
}
