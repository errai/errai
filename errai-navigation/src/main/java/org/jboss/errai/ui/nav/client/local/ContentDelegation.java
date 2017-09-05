package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

public interface ContentDelegation {

    void showContent(PageNode<Object> page, NavigatingContainer container, IsWidget widget);

    void hideContent(PageNode<Object> page, NavigatingContainer container, IsWidget widget);
}
