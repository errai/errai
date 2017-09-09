package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.api.DelegationControl;

/**
 * Content delegation control interface.
 * @author Ben Dol
 */
public interface ContentDelegation {

    void showContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object previousPage,
                     DelegationControl control);

    void hideContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object nextPage,
                     DelegationControl control);
}
