package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field holds information about the state of the
 * current page. The navigation framework writes state information from the
 * history token to the field when navigating to the page.
 * <p>
 * The target field must be one of the following types that are supported by the
 * navigation system:
 * <ul>
 * <li>A primitive type (other than char): boolean, byte, short, int, long,
 * float, or double
 * <li>A boxed primitive type (other than Character): Boolean, Byte, Short,
 * Integer, Long, Float, or Double
 * <li>String
 * <li>A collection of any of the above (the field type must be
 * {@code Collection<T>}, {@code List<T>}, or {@code Set<T>} where {@code T} is
 * a boxed primitive or String).
 * </ul>
 *
 * @see Page
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface PageState {

}
