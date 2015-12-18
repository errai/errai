package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of the annotated class should not be sent over the wire when fired as
 * Errai CDI Events.
 * 
 * This annotation is only useful in conjunction with @Portable.
 * 
 * @since Errai 3.0
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocalEvent {

}
