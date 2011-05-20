package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JParameter;
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

    public JavaReflectionMethod(Method method) {
        this.method = method;
    }

    public String getName() {
        return method.getName();
    }

    public MetaParameter[] getParameters() {
        List<MetaParameter> parmList = new ArrayList<MetaParameter>();

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            parmList.add(new JavaReflectionParameter(method.getParameterTypes()[i],
                    method.getParameterAnnotations()[i]));
        }

        return parmList.toArray(new MetaParameter[parmList.size()]);
    }

    public MetaClass getReturnType() {
        return new JavaReflectionClass(method.getReturnType());
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
        return new JavaReflectionClass(method.getDeclaringClass());
    }
}
