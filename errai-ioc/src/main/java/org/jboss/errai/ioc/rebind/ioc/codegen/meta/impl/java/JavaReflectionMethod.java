package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JavaReflectionMethod extends MetaMethod {
    private Method method;
    private MetaParameter[] parameters;
    private MetaClass declaringClass;
    private MetaClass returnType;

    JavaReflectionMethod(Method method) {
        this.method = method;

        List<MetaParameter> parmList = new ArrayList<MetaParameter>();

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            parmList.add(new JavaReflectionParameter(method.getParameterTypes()[i],
                    method.getParameterAnnotations()[i], this));
        }

        parameters = parmList.toArray(new MetaParameter[parmList.size()]);

        declaringClass = MetaClassFactory.get(method.getDeclaringClass());
        returnType = MetaClassFactory.get(method.getReturnType());
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

    @Override
    public MetaType getGenericReturnType() {
        return JavaReflectionUtil.fromType(method.getGenericReturnType());
    }

    @Override
    public MetaType[] getGenericParameterTypes() {
        return JavaReflectionUtil.fromTypeArray(method.getGenericParameterTypes());
    }

    public MetaTypeVariable[] getTypeParameters() {
        return JavaReflectionUtil.fromTypeVariable(method.getTypeParameters());
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

    public boolean isAbstract() {
        return (method.getModifiers() & Modifier.ABSTRACT) != 0;
    }

    public boolean isPublic() {
        return (method.getModifiers() & Modifier.PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (method.getModifiers() & Modifier.PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (method.getModifiers() & Modifier.PROTECTED) != 0;
    }

    public boolean isFinal() {
        return (method.getModifiers() & Modifier.FINAL) != 0;
    }

    public boolean isStatic() {
        return (method.getModifiers() & Modifier.STATIC) != 0;
    }

    public boolean isTransient() {
        return (method.getModifiers() & Modifier.TRANSIENT) != 0;
    }

    public boolean isSynthetic() {
        return method.isSynthetic();
    }

    public boolean isSynchronized() {
        return (method.getModifiers() & Modifier.SYNCHRONIZED) != 0;
    }
}
