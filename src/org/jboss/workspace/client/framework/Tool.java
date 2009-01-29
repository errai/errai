package org.jboss.workspace.client.framework;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.rpc.StatePacket;

public interface Tool {
    public Widget getWidget(StatePacket packet);

    public String getName();

    public String getId();

    public Image getIcon();

    public boolean multipleAllowed();
}
