package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MetaParameter {
    public String getName();

    public MetaClass getType();

    public Annotation[] getAnnotations();

    public MetaClassMember getDeclaringMember();
}
