package org.jboss.errai.bus.server.annotations;

import org.jboss.errai.bus.client.protocols.MessageParts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mike Brock .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface MessageParameter {
    String value() default "Value";
}
