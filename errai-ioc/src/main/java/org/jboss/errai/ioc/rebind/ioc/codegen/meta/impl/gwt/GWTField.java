package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTField extends MetaField {
    private JField field;
    private Annotation[] annotations;

    GWTField(JField field) {
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
        return MetaClassFactory.get(field.getType());
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

    @Override
    public MetaType getGenericType() {
        JGenericType genericType = field.getType().isGenericType();
        if (genericType != null) {
            return new GWTGenericDeclaration(genericType);
        }
        return null;
    }


    public MetaClass getDeclaringClass() {
        return MetaClassFactory.get(field.getEnclosingType());
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isPublic() {
        return field.isPublic();
    }

    public boolean isPrivate() {
        return field.isPrivate();
    }

    public boolean isProtected() {
        return field.isProtected();
    }

    public boolean isFinal() {
        return field.isFinal();
    }

    public boolean isStatic() {
        return field.isStatic();
    }

    public boolean isTransient() {
        return field.isTransient();
    }

    public boolean isSynthetic() {
        return false;
    }

    public boolean isSynchronized() {
        return false;
    }
}
