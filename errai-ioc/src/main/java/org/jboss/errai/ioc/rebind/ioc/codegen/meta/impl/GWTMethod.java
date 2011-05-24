package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.HasAnnotations;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTMethod implements MetaMethod {
    private JMethod method;
    private Annotation[] annotations;

    public GWTMethod(JMethod method) {
        this.method = method;

        try {
            Class<?> cls = Class.forName(method.getEnclosingType().getQualifiedSourceName(), false,
                    Thread.currentThread().getContextClassLoader());

            Method meth = cls.getMethod(method.getName(), InjectUtil.jParmToClass(method.getParameters()));

            annotations = meth.getAnnotations();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return method.getName();
    }

    public MetaClass getReturnType() {
        return new GWTClass(method.getReturnType());
    }

    public MetaParameter[] getParameters() {
        List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

        for (JParameter jParameter : method.getParameters()) {
            parameterList.add(new GWTParameter(jParameter, this));
        }

        return parameterList.toArray(new MetaParameter[parameterList.size()]);
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

    public MetaClass getDeclaringClass() {
        return new GWTClass(method.getEnclosingType());
    }
}
