package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTMethod extends MetaMethod {
    private JMethod method;
    private Annotation[] annotations;

    GWTMethod(JMethod method) {
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
        return MetaClassFactory.get(method.getReturnType());
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
        return MetaClassFactory.get(method.getEnclosingType());
    }

    @Override
    public MetaType getGenericReturnType() {
        JGenericType type = method.getReturnType().isGenericType();
        if (type != null) {
            return new GWTGenericDeclaration(type);
        }
        return null;
    }

    @Override
    public MetaType[] getGenericParameterTypes() {
        return null;
    }

    public boolean isAbstract() {
        return method.isAbstract();
    }

    public boolean isPublic() {
        return method.isPublic();
    }

    public boolean isPrivate() {
        return method.isPrivate();
    }

    public boolean isProtected() {
        return method.isProtected();
    }

    public boolean isFinal() {
        return method.isFinal();
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public boolean isTransient() {
        return false;
    }

    public boolean isSynthetic() {
        return false;
    }

    public boolean isSynchronized() {
        return false;
    }

    public MetaTypeVariable[] getTypeParameters() {
        return GWTUtil.fromTypeVariable(method.getTypeParameters());
    }
}
