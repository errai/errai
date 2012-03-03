package org.jboss.errai.demo.mobile.client.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * CDI event qualifier indicating that the OrientationEvent received is the last
 * one for the client it describes. Clients will find this useful for cleaning
 * up the DOM.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface Disconnected {
}
