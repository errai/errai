package org.jboss.errai.cdi.client.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Mike Brock .
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface Conversational {
}
