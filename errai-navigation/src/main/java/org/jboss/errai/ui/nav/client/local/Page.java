package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the target type,
 * <em>which must be a subtype of Widget or an implementation of IsWidget,</em>
 * is a named page with optional state information within the Errai Navigation
 * system.
 * 
 * @see TransitionTo
 * @see PageState
 * @see PageRole
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
   * Defines the roles of the page. You can group pages together by defining roles
   * that extend either {@link PageRole} or {@link UniquePageRole} a example of this is
   * the {@link DefaultPage} indicating that this page is the starting page.
   *
   * @return the roles that this page belongs to
   */
  Class<? extends PageRole>[] role() default {};
}
