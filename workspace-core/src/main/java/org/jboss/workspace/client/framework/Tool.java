package org.jboss.workspace.client.framework;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public interface Tool {

    public Widget getWidget();

    public String getName();

    public String getId();

    public Image getIcon();

    public boolean multipleAllowed();
}
