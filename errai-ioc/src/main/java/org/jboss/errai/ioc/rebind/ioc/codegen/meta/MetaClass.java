package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import com.google.gwt.core.ext.typeinfo.JClassType;

public interface MetaClass extends HasAnnotations {
    public String getName();

    public String getFullyQualifedName();

    public MetaMethod[] getMethods();

    public MetaMethod[] getDeclaredMethods();

    public MetaMethod getMethod(String name, Class... parameters);

    public MetaMethod getDeclaredMethod(String name, Class... parameters);

    public MetaMethod getDeclaredMethod(String name, MetaClass... parameters);

    public MetaField[] getFields();

    public MetaField[] getDeclaredFields();

    public MetaField getField(String name);

    public MetaField getDeclaredField(String name);

    public MetaConstructor[] getConstructors();

    public MetaConstructor[] getDeclaredConstructors();

    public MetaConstructor getConstructor(Class... parameters);

    public MetaConstructor getConstructor(MetaClass... parameters);

    public MetaConstructor getDeclaredConstructor(Class... parameters);
    
    public MetaClass[] getParameterizedTypes();

    public MetaClass[] getInterfaces();

    public MetaClass getSuperClass();

    public MetaClass getComponentType();

    public boolean isAssignableFrom(MetaClass clazz);

    public boolean isAssignableTo(MetaClass clazz);

    public boolean isAssignableFrom(Class clazz);

    public boolean isAssignableTo(Class clazz);

    public boolean isAssignableFrom(JClassType clazz);

    public boolean isAssignableTo(JClassType clazz);

    public boolean isInterface();

    public boolean isAbstract();

    public boolean isArray();

    public boolean isEnum();

    public boolean isAnnotation();

    public boolean isPublic();

    public boolean isPrivate();

    public boolean isProtected();

    public boolean isFinal();

    public boolean isStatic();
}
