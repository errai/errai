package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the target type, <em>which must be a subtype of Widget</em> is
 * a named page with optional state information within the Errai Navigation
 * system.
 *
 * @see TransitionTo
 * @see PageState
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Page {

  /**
   * The path component that identifies this page. If not specified, the page's
   * name will be the simple name of the class.
   */
  String path() default "";

  /**
   * Indicates that the target class is the default starting page for the
   * application. This means the page can be reached from the empty path ("") as
   * well as the path it would normally be reachable by according to the
   * {@link #path()} specification.
   * <p>
   * In an Errai application that uses the navigation system, exactly one
   * {@code @Page}-annotated class must have {@code startingPage} set to
   * {@code true}. It is a compile-time error otherwise.
   */
  boolean startingPage() default false;
}
