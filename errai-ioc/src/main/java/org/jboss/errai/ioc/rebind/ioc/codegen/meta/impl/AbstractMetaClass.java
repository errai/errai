package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.HasAnnotations;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
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

    public MetaConstructor getConstructor(MetaClass... parameters) {
        return _getConstructor(getConstructors(), parameters);
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

    public boolean isAssignableFrom(MetaClass clazz) {
        MetaClass cls = clazz;
        do {
            if (cls.equals(clazz)) return true;
        } while ((cls = cls.getSuperClass()) != null);

        return _hasInterface(clazz.getInterfaces(), this);
    }

    public boolean isAssignableTo(MetaClass clazz) {
        MetaClass cls = this;
        do {
            if (cls.equals(this)) return true;
        } while ((cls = cls.getSuperClass()) != null);


        return _hasInterface(getInterfaces(), clazz);
    }

    private static boolean _hasInterface(MetaClass[] from, MetaClass to) {
        for (MetaClass iface : from) {
            if (to.equals(iface)) return true;
            else if (_hasInterface(iface.getInterfaces(), to)) return true;
        }

        return false;
    }

    public boolean isAssignableFrom(Class clazz) {
        return isAssignableFrom(MetaClassFactory.get(clazz));
    }

    public boolean isAssignableTo(Class clazz) {
        return isAssignableTo(MetaClassFactory.get(clazz));
    }

    public boolean isAssignableTo(JClassType clazz) {
        return isAssignableFrom(MetaClassFactory.get(clazz));
    }

    public boolean isAssignableFrom(JClassType clazz) {
        return isAssignableTo(MetaClassFactory.get(clazz));
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
