package org.jboss.errai.client.framework;

import com.google.gwt.user.client.ui.Image;

public interface Tool extends WSComponent {

    public String getName();

    public String getId();

    public Image getIcon();

    public boolean multipleAllowed();
}
