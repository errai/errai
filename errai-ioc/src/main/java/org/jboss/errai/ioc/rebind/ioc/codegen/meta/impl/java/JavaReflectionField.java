package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class JavaReflectionField implements MetaField {
    private Field field;

    public JavaReflectionField(Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public MetaClass getType() {
        return new JavaReflectionClass(field.getType());
    }

    public Annotation[] getAnnotations() {
        return field.getAnnotations();
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
