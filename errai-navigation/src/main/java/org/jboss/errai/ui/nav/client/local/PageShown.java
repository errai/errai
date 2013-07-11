package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that the target method should be called when the {@code @Page}
 * widget it is a member of was displayed in the navigation content panel:
 * after the widget's {@code @PageState} fields have been updated and
 * before it is displayed in the navigation content panel.
 * <p>
 * When the client-side application is bootstrapping (the page is loading in the
 * browser), the Navigation system waits until all Errai modules are fully
 * initialized before displaying the initial page. Hence, it is safe to make RPC
 * requests and to fire portable CDI events from within a {@code @PageShown}
 * method.
 * <p>
 * The target method is permitted an optional parameter of type
 * {@link HistoryToken}. If the parameter is present, the framework will pass in
 * the history token that caused the page to show. This is useful in cases where
 * not all history token key names are known at compile time, so
 * {@code @PageState} fields can't be declared to accept their values.
 * <p>
 * The target method's return type must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or
 * private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see PageState
 * @see Navigation
 * @see PageShowing
 * @author Daniel Sachse <mail@w0mb.at>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageShown {

}
