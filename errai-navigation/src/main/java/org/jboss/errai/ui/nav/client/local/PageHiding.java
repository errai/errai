package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.ui.nav.client.local.api.NavigationControl;

/**
 * Indicates that the target method should be called when the {@code @Page} widget it belongs to is
 * about to be removed from the navigation content panel.
 * <p>
 * The target method is permitted an optional parameter of type {@link NavigationControl}. If the
 * parameter is present, the page navigation will not be carried out until
 * {@link NavigationControl#proceed()} is invoked. This is useful for interrupting page navigations
 * and then resuming at a later time (for example, to prompt the user to save their work before
 * transitioning to a new page).
 * <p>
 * The target method's return type will must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see Navigation
 * @see PageHidden
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageHiding {

}
