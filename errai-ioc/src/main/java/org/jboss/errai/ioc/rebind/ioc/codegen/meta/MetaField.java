package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import java.lang.annotation.Annotation;

public interface MetaField extends HasAnnotations {
    public MetaClass getType();
    public String getName();
    public Annotation[] getAnnotations();
}
