package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
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

    private GWTClass(JType classType) {
        super(classType);
    }

    public static MetaClass newInstance(JType type) {
        return MetaClassFactory.get(type);
    }

    public static MetaClass newUncachedInstance(JType type) {
        return new GWTClass(type);
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
        JClassType type = getEnclosedMetaObject().isClassOrInterface();
        if (type == null) {
            return null;
        }

        return fromMethodArray(type.getMethods());
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
        JClassType type = getEnclosedMetaObject().isClassOrInterface();
        if (type == null) {
            return null;
        }
        return fromFieldArray(type.getFields());
    }

    public MetaField[] getDeclaredFields() {
        return getFields();
    }

    public MetaField getField(String name) {
        JClassType type = getEnclosedMetaObject().isClassOrInterface();
        if (type == null) {
            return null;
        }

        JField field = type.getField(name);

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
        JClassType type = getEnclosedMetaObject().isClassOrInterface();
        if (type == null) {
            return null;
        }

        return fromMethodArray(type.getConstructors());
    }

    public MetaConstructor[] getDeclaredConstructors() {
        return getConstructors();
    }

    public MetaClass[] getInterfaces() {
        List<MetaClass> metaClassList = new ArrayList<MetaClass>();
        for (JClassType type : getEnclosedMetaObject().isClassOrInterface()
                .getImplementedInterfaces()) {

            metaClassList.add(MetaClassFactory.get(type));
        }

        return metaClassList.toArray(new MetaClass[metaClassList.size()]);
    }

    public MetaClass getSuperClass() {
        JClassType type = getEnclosedMetaObject().isClassOrInterface();
        if (type == null) {
            return null;
        }
        return MetaClassFactory.get(type.getEnclosingType());
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

    public boolean isInterface() {
        return getEnclosedMetaObject().isInterface() != null;
    }

    public boolean isAbstract() {
        return getEnclosedMetaObject().isClass() != null && getEnclosedMetaObject().isClass().isAbstract();
    }


    public boolean isEnum() {
        return getEnclosedMetaObject().isEnum() != null;
    }

    public boolean isAnnotation() {
        return getEnclosedMetaObject().isAnnotation() != null;
    }

    public boolean isPublic() {
        return getEnclosedMetaObject().isClassOrInterface() != null &&
                getEnclosedMetaObject().isClassOrInterface().isPublic();
    }

    public boolean isPrivate() {
        return getEnclosedMetaObject().isClassOrInterface() != null &&
                getEnclosedMetaObject().isClassOrInterface().isPrivate();
    }

    public boolean isProtected() {
        return getEnclosedMetaObject().isClassOrInterface() != null &&
                getEnclosedMetaObject().isClassOrInterface().isProtected();
    }

    public boolean isFinal() {
        return getEnclosedMetaObject().isClassOrInterface() != null &&
                getEnclosedMetaObject().isClassOrInterface().isFinal();
    }

    public boolean isStatic() {
        return getEnclosedMetaObject().isClassOrInterface() != null &&
                getEnclosedMetaObject().isClassOrInterface().isStatic();
    }
}
