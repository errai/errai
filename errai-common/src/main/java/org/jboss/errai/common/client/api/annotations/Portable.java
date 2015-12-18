package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.Documented;
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
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Portable {
  /**
   * Indicate that the annotated class should be treated as an alias of an existing marshalling mapping, and should
   * not be directly mapped itself.
   * @return
   */
  Class<?> aliasOf() default Object.class;
}
