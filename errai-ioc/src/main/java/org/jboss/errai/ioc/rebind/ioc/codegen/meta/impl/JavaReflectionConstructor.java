package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.JavaReflectionParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionConstructor extends AbstractMetaMember implements MetaConstructor {
    private Constructor constructor;
    private MetaParameter[] parameters;
    private MetaClass declaringClass;
    private Annotation[] annotationsCache;

    public JavaReflectionConstructor(Constructor c) {
        constructor = c;

        List<MetaParameter> parmList = new ArrayList<MetaParameter>();

        for (int i = 0; i < c.getParameterTypes().length; i++) {
            parmList.add(new JavaReflectionParameter(c.getParameterTypes()[i],
                    c.getParameterAnnotations()[i], this));
        }

        parameters = parmList.toArray(new MetaParameter[parmList.size()]);
        declaringClass = new JavaReflectionClass(c.getDeclaringClass());
    }

    public MetaParameter[] getParameters() {
        return parameters;
    }

    public MetaClass getDeclaringClass() {
        return declaringClass;
    }

    public Annotation[] getAnnotations() {
        if (annotationsCache == null) {
            annotationsCache = constructor.getAnnotations() ;
        }
        return annotationsCache;
    }
}
