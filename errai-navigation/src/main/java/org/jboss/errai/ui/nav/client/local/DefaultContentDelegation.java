package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

public class DefaultContentDelegation implements ContentDelegation {

    @Override
    public void showContent(PageNode<Object> page, NavigatingContainer container, IsWidget widget) {
        container.setWidget(widget);
    }

    @Override
    public void hideContent(PageNode<Object> page, NavigatingContainer container, IsWidget widget) {
        container.clear();
    }
}
