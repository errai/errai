package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.mvel2.util.ParseTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractMetaClass<T> extends MetaClass {
    private final T enclosedMetaObject;
    protected MetaParameterizedType parameterizedType;

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

    public MetaMethod getMethod(String name, MetaClass... parameters) {
        return _getMethod(getMethods(), name, parameters);
    }

    public MetaMethod getDeclaredMethod(String name, Class... parmTypes) {
        return _getMethod(getDeclaredMethods(), name, InjectUtil.classToMeta(parmTypes));
    }

    public MetaMethod getDeclaredMethod(String name, MetaClass... parmTypes) {
        return _getMethod(getDeclaredMethods(), name, parmTypes);
    }

    public MetaMethod getBestMatchingMethod(String name, Class... parameters) {
        Class<?> cls = asClass();
        Method m = ParseTools.getBestCandidate(parameters, name, cls, cls.getMethods(), false);
        if (m == null) return null;

        MetaClass metaClass = MetaClassFactory.get(cls);
        return metaClass.getMethod(name, m.getParameterTypes());
    }

    public MetaMethod getBestMatchingMethod(String name, MetaClass... parameters) {
        return getBestMatchingMethod(name, MetaClassFactory.asClassArray(parameters));
    }

    public MetaConstructor getBestMatchingConstructor(Class... parameters) {
        Class<?> cls = asClass();
        Constructor c = ParseTools.getBestConstructorCandidate(parameters, cls, false);
        if (c == null) return null;

        MetaClass metaClass = MetaClassFactory.get(cls);
        return metaClass.getConstructor(c.getParameterTypes());
    }

    public MetaConstructor getBestMatchingConstructor(MetaClass... parameters) {
        return getBestMatchingConstructor(MetaClassFactory.asClassArray(parameters));
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
            hashString = MetaClass.class.getName() + ":" + getFullyQualifedName();
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
    public MetaParameterizedType getParameterizedType() {
        return parameterizedType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MetaClass && hashString().equals(MetaClass.class.getName() + ":" + ((MetaClass) o).getFullyQualifedName());
    }

    @Override
    public int hashCode() {
        return hashString().hashCode();
    }

    public Class<?> asClass() {
        if (enclosedMetaObject instanceof Class) {
            return (Class<?>) enclosedMetaObject;
        } else {
            try {
                return Class.forName(((JClassType) enclosedMetaObject).getQualifiedSourceName(), false,
                        Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
