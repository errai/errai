package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field holds information about the state of the
 * current page. This state information will be remembered when navigating away
 * from the page and restored when navigating back to the page.
 *
 * @see Page
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface PageState {

}
