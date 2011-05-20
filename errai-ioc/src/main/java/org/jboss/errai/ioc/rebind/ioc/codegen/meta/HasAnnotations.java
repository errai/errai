package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface HasAnnotations {
    public Annotation[] getAnnotations();

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation);

    public Annotation getAnnotation(Class<? extends Annotation> annotation);
}
