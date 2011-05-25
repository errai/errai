package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

public interface MetaClass extends HasAnnotations {
    public String getName();

    public String getFullyQualifedName();

    public MetaMethod[] getMethods();

    public MetaMethod[] getDeclaredMethods();

    public MetaMethod getMethod(String name, Class... parameters);

    public MetaMethod getDeclaredMethod(String name, Class... parameters);

    public MetaField[] getFields();

    public MetaField[] getDeclaredFields();

    public MetaField getField(String name);

    public MetaField getDeclaredField(String name);

    public MetaConstructor[] getConstructors();

    public MetaConstructor[] getDeclaredConstructors();

    public MetaConstructor getConstructor(Class... parameters);

    public MetaConstructor getDeclaredConstructor(Class... parameters);
    
    public MetaClass[] getParameterizedTypes();
}
