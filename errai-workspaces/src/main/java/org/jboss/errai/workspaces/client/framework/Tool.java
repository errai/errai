package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.user.client.ui.Image;
import org.jboss.errai.common.client.framework.WSComponent;

public interface Tool extends WSComponent {
    public String getName();

    public String getId();

    public Image getIcon();

    public boolean multipleAllowed();
}
