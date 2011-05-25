package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.JavaReflectionParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaReflectionMethod implements MetaMethod {
    private Method method;
    private MetaParameter[] parameters;
    private MetaClass declaringClass;
    private MetaClass returnType;

    public JavaReflectionMethod(Method method) {
        this.method = method;

        List<MetaParameter> parmList = new ArrayList<MetaParameter>();

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            parmList.add(new JavaReflectionParameter(method.getParameterTypes()[i],
                    method.getParameterAnnotations()[i], this));
        }

        parameters = parmList.toArray(new MetaParameter[parmList.size()]);

        declaringClass = new JavaReflectionClass(method.getDeclaringClass());
        returnType = new JavaReflectionClass(method.getReturnType());
    }

    public String getName() {
        return method.getName();
    }

    public MetaParameter[] getParameters() {
        return parameters;
    }

    public MetaClass getReturnType() {
        return returnType;
    }

    public Annotation[] getAnnotations() {
        return method.getAnnotations();
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

    public MetaClass getDeclaringClass() {
        return declaringClass;
    }
}
