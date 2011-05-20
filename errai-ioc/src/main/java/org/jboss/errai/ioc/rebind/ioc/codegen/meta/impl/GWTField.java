package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JField;
import com.sun.xml.internal.rngom.ast.builder.Annotations;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTField implements MetaField {
    private JField field;
    private Annotation[] annotations;

    public GWTField(JField field) {
        this.field = field;

        try {
            Class<?> cls = Class.forName(field.getEnclosingType().getQualifiedSourceName(), false,
                    Thread.currentThread().getContextClassLoader());

            Field fld = cls.getField(field.getName());

            annotations = fld.getAnnotations();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public MetaClass getType() {
        return new GWTClass(field.getType());
    }

    public String getName() {
        return field.getName();
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
