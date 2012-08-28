package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target class (which must also be annotated with
 * {@link Page @Page}) is the default page for the application. This means the
 * page can be reached from the empty path ("") as well as the path it would
 * normally be reachable by in the absence of the {@code @DefaultPage}
 * annotation.
 * <p>
 * In an Errai application that uses the navigation system, exactly one page
 * must be annotated with {@code @DefaultPage}. It is a compile-time error
 * otherwise.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultPage {

}
