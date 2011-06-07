/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;

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

    public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
        for (Annotation a : getAnnotations()) {
            if (a.annotationType().equals(annotation)) return (A) a;
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
