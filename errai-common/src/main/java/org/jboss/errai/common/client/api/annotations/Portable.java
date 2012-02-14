package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of the annotated class are eligible to be serialized
 * and sent over the wire between server and clients.
 * <p>
 * 
 * @since Errai 2.0
 * @author Mike Brock <cbrock@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Portable {
  Class<?> aliasOf() default Object.class;
}
