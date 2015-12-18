package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of the annotated class are not eligible to be serialized and sent over the wire between
 * server and clients.
 * <p>
 * This annotation is only expected to be useful for classes nested in a portable class: nested classes are portable by
 * default, unless annotated with {@code @NotPortable}.
 * 
 * @since Errai 2.0
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NonPortable {
  
}
