package org.jboss.errai.jpa.client.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target type should receive JPA entity lifecycle events for
 * all entity types. Note that this automatic global registration only works in
 * client-side code. For server-side code, you need to register the listener
 * class using entity-listener entries in persistence.xml.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalEntityListener {

}
