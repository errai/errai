package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

public class DefaultContentDelegation implements ContentDelegation {

    @Override
    public void showContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, PageNode<?> previousPage,
                            NavigationControl control) {
        defaultContainer.setWidget(widget);
        control.proceed();
    }

    @Override
    public void hideContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, PageNode<?> nextPage,
                            NavigationControl control) {
        defaultContainer.clear();
        control.proceed();
    }
}
