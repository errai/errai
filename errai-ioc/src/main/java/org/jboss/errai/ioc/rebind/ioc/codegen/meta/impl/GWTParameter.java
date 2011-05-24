package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JAbstractMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassMember;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameter implements MetaParameter {
    private JParameter parameter;
    private Annotation[] annotations;
    private MetaClassMember declaredBy;

    public GWTParameter(JParameter parameter, MetaClassMember declaredBy) {
        this.parameter = parameter;
        this.declaredBy = declaredBy;

        try {
            Class<?> cls = Class.forName(parameter.getEnclosingMethod().getEnclosingType().getQualifiedSourceName(),
                    false, Thread.currentThread().getContextClassLoader());

            JAbstractMethod jMethod = parameter.getEnclosingMethod();

            int index = -1;
            for (int i = 0; i < jMethod.getParameters().length; i++) {
                if (jMethod.getParameters()[i].getName().equals(parameter.getName())) {
                    index = i;
                }
            }

            Method method = cls.getMethod(jMethod.getName(),
                    InjectUtil.jParmToClass(jMethod.getParameters()));

            annotations = method.getParameterAnnotations()[index];

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return parameter.getName();
    }

    public MetaClass getType() {
        return new GWTClass(parameter.getType());
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public MetaClassMember getDeclaringMember() {
        return declaredBy;
    }
}
