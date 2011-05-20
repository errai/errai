package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaReflectionClass implements MetaClass {
    private Class clazz;

    public JavaReflectionClass(Class clazz) {
        this.clazz = clazz;
    }

    public String getName() {
        return clazz.getName();
    }

    private static MetaMethod[] fromMethodArray(Method[] methods) {
        List<MetaMethod> methodList = new ArrayList<MetaMethod>();

        for (Method m : methods) {
            methodList.add(new JavaReflectionMethod(m));
        }

        return methodList.toArray(new MetaMethod[methodList.size()]);
    }

    public MetaMethod[] getMethods() {
        return fromMethodArray(clazz.getMethods());
    }

    public MetaMethod[] getDeclaredMethods() {
        return fromMethodArray(clazz.getDeclaredMethods());
    }

    private static MetaField[] fromFieldArray(Field[] methods) {
        List<MetaField> methodList = new ArrayList<MetaField>();

        for (Field f : methods) {
            methodList.add(new JavaReflectionField(f));
        }

        return methodList.toArray(new MetaField[methodList.size()]);
    }

    public MetaField[] getFields() {
        return fromFieldArray(clazz.getFields());
    }

    public MetaField[] getDeclaredFields() {
        return fromFieldArray(clazz.getDeclaredFields());
    }


}
