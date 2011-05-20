package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

import java.lang.reflect.Field;

public class JavaReflectionField implements MetaField {
    private Field field;

    public JavaReflectionField(Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }
}
