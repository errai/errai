package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import java.lang.annotation.Annotation;

public interface MetaParameter {
    public String getName();
    public MetaClass getType();
    public Annotation[] getAnnotations();
}
