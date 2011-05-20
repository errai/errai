package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.lang.reflect.Method;

public class JavaReflectionMethod implements MetaMethod {
    private Method method;

    public JavaReflectionMethod(Method method) {
        this.method = method;
    }

    public String getName() {
        return method.getName();
    }
}
