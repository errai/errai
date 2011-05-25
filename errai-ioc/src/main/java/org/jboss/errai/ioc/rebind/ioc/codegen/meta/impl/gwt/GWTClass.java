package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.AbstractMetaClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class GWTClass extends AbstractMetaClass<JType> {
    private Annotation[] annotationsCache;

    public GWTClass(JType classType) {
        super(classType);
    }

    public String getName() {
        return getEnclosedMetaObject().getSimpleSourceName();
    }

    public String getFullyQualifedName() {
        return getEnclosedMetaObject().getQualifiedSourceName();
    }

    private static MetaMethod[] fromMethodArray(JMethod[] methods) {
        List<MetaMethod> methodList = new ArrayList<MetaMethod>();

        for (JMethod m : methods) {
            methodList.add(new GWTMethod(m));
        }

        return methodList.toArray(new MetaMethod[methodList.size()]);
    }

    public MetaMethod[] getMethods() {
        return fromMethodArray(getEnclosedMetaObject().isClassOrInterface().getMethods());
    }

    public MetaMethod[] getDeclaredMethods() {
        return getMethods();
    }

    private static MetaField[] fromFieldArray(JField[] methods) {
        List<MetaField> methodList = new ArrayList<MetaField>();

        for (JField f : methods) {
            methodList.add(new GWTField(f));
        }

        return methodList.toArray(new MetaField[methodList.size()]);
    }

    public MetaField[] getFields() {
        return fromFieldArray(getEnclosedMetaObject().isClassOrInterface().getFields());
    }

    public MetaField[] getDeclaredFields() {
        return getFields();
    }


    public MetaField getField(String name) {
        JField field = getEnclosedMetaObject().isClassOrInterface().getField(name);

        if (field == null) {
            throw new RuntimeException("no such field: " + field);
        }

        return new GWTField(field);
    }

    public MetaField getDeclaredField(String name) {
        return getField(name);
    }

    private static MetaConstructor[] fromMethodArray(JConstructor[] constructors) {
        List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

        for (JConstructor c : constructors) {
            constructorList.add(new GWTConstructor(c));
        }

        return constructorList.toArray(new MetaConstructor[constructorList.size()]);
    }

    public MetaConstructor[] getConstructors() {
        return fromMethodArray(getEnclosedMetaObject().isClassOrInterface().getConstructors());
    }

    public MetaConstructor[] getDeclaredConstructors() {
        return getConstructors();
    }

    public Annotation[] getAnnotations() {
        if (annotationsCache == null) {
            try {
                Class<?> cls = Class.forName(getEnclosedMetaObject().getQualifiedSourceName(), false,
                        Thread.currentThread().getContextClassLoader());

                annotationsCache = cls.getAnnotations();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return annotationsCache;
    }

    public MetaClass[] getParameterizedTypes() {
        MetaClass[] parameterizedTypes = new MetaClass[1];
        JParameterizedType paramType = getEnclosedMetaObject().isParameterized();
        if (paramType != null)
            parameterizedTypes[0] = new GWTClass(paramType);

        return parameterizedTypes;
    }
}
