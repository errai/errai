package org.jboss.errai.demo.mobile.client.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * CDI event qualifier indicating that the OrientationEvent received is part of
 * an ongoing series of events for the client it describes. When and if the
 * client in question goes away, a final event for that client for that client
 * qualified by {@link Disconnected} for that client.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface Ongoing {
}
