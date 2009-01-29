package org.jboss.workspace.client;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public interface Component {
    public boolean newTab();

    public Image getIcon();
    public String getTabName();

    public Widget getWidget();
}
