package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import java.lang.annotation.Annotation;

public interface MetaClass {
    public String getName();

    public String getFullyQualifedName();

    public MetaMethod[] getMethods();

    public MetaMethod[] getDeclaredMethods();

    public MetaField[] getFields();

    public MetaField[] getDeclaredFields();

    public MetaMethod getMethod(String name, Class... parameters);

    public MetaMethod getDeclaredMethod(String name, Class... parameters);

    public Annotation[] getAnnotations();
}
