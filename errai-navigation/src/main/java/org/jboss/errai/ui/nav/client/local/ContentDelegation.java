package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.api.DelegationControl;

/**
 * Content delegation control interface.
 *
 * @author Ben Dol
 */
public interface ContentDelegation {

    /**
     * Called when the page is showing its content (setting the container widget).
     *
     * @param page the current page being shown.
     * @param defaultContainer the default content container.
     * @param widget the widget reference object for the page.
     * @param previousPage the previous page, <b>this can be null</b>.
     * @param control the delegation control for proceeding navigation process.
     */
    void showContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object previousPage,
                     DelegationControl control);

    /**
     * Called when the page is hiding its content (clearing container).
     *
     * @param page the current page being hidden.
     * @param defaultContainer the default content container.
     * @param widget the widget reference object for the page.
     * @param nextPage potential next requested page, <b>this can be null</b>.
     * @param control the delegation control for proceeding navigation process.
     */
    void hideContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object nextPage,
                     DelegationControl control);
}
