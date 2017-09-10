package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.api.DelegationControl;

/**
 * Default content delegation procedure.
 * @author Ben Dol
 */
public class DefaultContentDelegation implements ContentDelegation {

    @Override
    public void showContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object previousPage,
                            DelegationControl control) {
        defaultContainer.setWidget(widget);
        control.proceed();
    }

    @Override
    public void hideContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object nextPage,
                            DelegationControl control) {
        defaultContainer.clear();
        control.proceed();
    }
}
