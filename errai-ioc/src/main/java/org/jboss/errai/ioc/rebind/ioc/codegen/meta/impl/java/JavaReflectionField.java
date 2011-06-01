package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JavaReflectionField extends MetaField {
    private Field field;

    JavaReflectionField(Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public MetaClass getType() {
        return MetaClassFactory.get(field.getType());
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

    @Override
    public MetaType getGenericType() {
        return JavaReflectionUtil.fromType(field.getGenericType());
    }

    public MetaClass getDeclaringClass() {
        return MetaClassFactory.get(field.getDeclaringClass());
    }

    public boolean isAbstract() {
        return (field.getModifiers() & Modifier.ABSTRACT) != 0;
    }

    public boolean isPublic() {
        return (field.getModifiers() & Modifier.PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (field.getModifiers() & Modifier.PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (field.getModifiers() & Modifier.PROTECTED) != 0;
    }

    public boolean isFinal() {
        return (field.getModifiers() & Modifier.FINAL) != 0;
    }

    public boolean isStatic() {
        return (field.getModifiers() & Modifier.STATIC) != 0;
    }

    public boolean isTransient() {
        return (field.getModifiers() & Modifier.TRANSIENT) != 0;
    }

    public boolean isSynthetic() {
        return field.isSynthetic();
    }

    public boolean isSynchronized() {
        return (field.getModifiers() & Modifier.SYNCHRONIZED) != 0;
    }
}
