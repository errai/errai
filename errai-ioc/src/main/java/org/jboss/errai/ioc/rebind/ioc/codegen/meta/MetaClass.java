package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

public interface MetaClass {
    public String getName();

    public MetaMethod[] getMethods();

    public MetaMethod[] getDeclaredMethods();

    public MetaField[] getFields();

    public MetaField[] getDeclaredFields();

}
