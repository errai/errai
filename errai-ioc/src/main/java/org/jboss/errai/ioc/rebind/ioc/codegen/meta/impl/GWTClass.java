package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.HasAnnotations;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class GWTClass extends AbstractMetaClass {
    private JType classType;
    private Annotation[] annotations;

    public GWTClass(JType classType) {
        this.classType = classType;

        try {
            Class<?> cls = Class.forName(classType.getQualifiedSourceName(), false,
                    Thread.currentThread().getContextClassLoader());

            annotations = cls.getAnnotations();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.hashString = "MetaClass:" + getFullyQualifedName();
    }

    public String getName() {
        return classType.getSimpleSourceName();
    }

    public String getFullyQualifedName() {
        return classType.getQualifiedSourceName();
    }

    private static MetaMethod[] fromMethodArray(JMethod[] methods) {
        List<MetaMethod> methodList = new ArrayList<MetaMethod>();

        for (JMethod m : methods) {
            methodList.add(new GWTMethod(m));
        }

        return methodList.toArray(new MetaMethod[methodList.size()]);
    }

    public MetaMethod[] getMethods() {
        return fromMethodArray(classType.isClassOrInterface().getMethods());
    }

    public MetaMethod[] getDeclaredMethods() {
        return fromMethodArray(classType.isClassOrInterface().getMethods());
    }

    private static MetaField[] fromFieldArray(JField[] methods) {
        List<MetaField> methodList = new ArrayList<MetaField>();

        for (JField f : methods) {
            methodList.add(new GWTField(f));
        }

        return methodList.toArray(new MetaField[methodList.size()]);
    }

    public MetaField[] getFields() {
        return fromFieldArray(classType.isClassOrInterface().getFields());
    }

    public MetaField[] getDeclaredFields() {
        return fromFieldArray(classType.isClassOrInterface().getFields());
    }

    private final String hashString;

    @Override
    public boolean equals(Object o) {
        return o instanceof MetaClass && hashString.equals("MetaClass:" + ((MetaClass) o).getFullyQualifedName());
    }

    @Override
    public int hashCode() {
        return hashString.hashCode();
    }
}
