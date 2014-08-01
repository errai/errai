package org.jboss.errai.ui.shared.api.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Configure LESS/CSS stylesheets used with an Errai App. Stylesheets specified are compiled and optimized in the order
 * defined.
 *
 * <p>There must never be more than one {@link StyleDescriptor} annotation on the classpath.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE })
public @interface StyleDescriptor {

  /**
   * An array of paths to LESS/CSS resources on the classpath. Relative paths are loaded relative to the class where the
   * {@link StyleDescriptor} annotation is found.
   */
  String[] value() default {};

}
