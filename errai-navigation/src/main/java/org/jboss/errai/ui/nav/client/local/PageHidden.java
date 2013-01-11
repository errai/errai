package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target method should be called after the {@code @Page}
 * widget it belongs has been removed from the navigation content
 * panel.
 * <p>
 * The target method must not take any parameters.
 * <p>
 * The target method's return type must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see Navigation
 * @see PageHiding
 * @author Daniel Sachse <mail@w0mb.at>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageHidden {

}
