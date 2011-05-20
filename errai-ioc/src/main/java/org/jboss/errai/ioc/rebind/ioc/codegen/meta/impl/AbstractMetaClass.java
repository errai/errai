package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.HasAnnotations;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractMetaClass implements MetaClass, HasAnnotations {
    protected Annotation[] annotations;

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

    public MetaMethod getMethod(String name, Class... parmTypes) {
        return _getMethod(getMethods(), name, InjectUtil.classToMeta(parmTypes));
    }

    public MetaMethod getDeclaredMethod(String name, Class... parmTypes) {
        return _getMethod(getDeclaredMethods(), name, InjectUtil.classToMeta(parmTypes));
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotation) {
        for (Annotation a : getAnnotations()) {
            if (a.annotationType().equals(annotation)) return a;
        }
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }
}
