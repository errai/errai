package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target method should be called when the {@code @Page}
 * widget it is a member of is about to be displayed in the navigation content
 * panel: after the widget's {@code @PageState} fields have been updated and
 * before it is displayed in the navigation content panel.
 * <p>
 * The target method is permitted two kinds of arguments, both optional: a
 * {@code String}-typed argument will receive the entire unparsed history token
 * that caused this page to display; a {@link HistoryToken}-typed argument will
 * receive the parsed version of the same information.
 * <p>
 * The target method's return type must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see PageState
 * @see Navigation
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageShowing {

}
