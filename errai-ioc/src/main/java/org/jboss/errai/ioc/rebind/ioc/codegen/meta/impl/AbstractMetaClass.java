package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.HasAnnotations;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractMetaClass<T> implements MetaClass, HasAnnotations {
    private final T enclosedMetaObject;

    protected AbstractMetaClass(T enclosedMetaObject) {
        this.enclosedMetaObject = enclosedMetaObject;
    }

    protected static MetaMethod _getMethod(MetaMethod[] methods, String name, MetaClass... parmTypes) {
        Outer:
        for (MetaMethod method : methods) {
            if (method.getName().equals(name) && method.getParameters().length == parmTypes.length) {
                for (int i = 0; i < parmTypes.length; i++) {
                    if (!method.getParameters()[i].getType().equals(parmTypes[i])) {
                        continue Outer;
                    }
                }
                return method;
            }
        }
        return null;
    }

    protected static MetaConstructor _getConstructor(MetaConstructor[] constructors, MetaClass... parmTypes) {
        Outer:
        for (MetaConstructor constructor : constructors) {
            if (constructor.getParameters().length == parmTypes.length) {
                for (int i = 0; i < parmTypes.length; i++) {
                    if (!constructor.getParameters()[i].getType().equals(parmTypes[i])) {
                        continue Outer;
                    }
                }
                return constructor;
            }
        }
        return null;
    }


    public MetaMethod getMethod(String name, Class... parmTypes) {
        return _getMethod(getMethods(), name, InjectUtil.classToMeta(parmTypes));
    }

    public MetaMethod getDeclaredMethod(String name, Class... parmTypes) {
        return _getMethod(getDeclaredMethods(), name, InjectUtil.classToMeta(parmTypes));
    }

    public MetaMethod getDeclaredMethod(String name, MetaClass... parmTypes) {
        return _getMethod(getDeclaredMethods(), name, parmTypes);
    }

    public MetaConstructor getConstructor(Class... parameters) {
        return _getConstructor(getConstructors(), InjectUtil.classToMeta(parameters));
    }

    public MetaConstructor getDeclaredConstructor(Class... parameters) {
        return _getConstructor(getDeclaredConstructors(), InjectUtil.classToMeta(parameters));
    }

    public final Annotation getAnnotation(Class<? extends Annotation> annotation) {
        for (Annotation a : getAnnotations()) {
            if (a.annotationType().equals(annotation)) return a;
        }
        return null;
    }

    public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }

    public T getEnclosedMetaObject() {
        return enclosedMetaObject;
    }

    private String hashString;

    private String hashString() {
        if (hashString == null) {
            hashString = "MetaClass:" + getFullyQualifedName();
        }
        return hashString;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MetaClass && hashString().equals("MetaClass:" + ((MetaClass) o).getFullyQualifedName());
    }

    @Override
    public int hashCode() {
        return hashString().hashCode();
    }


}
