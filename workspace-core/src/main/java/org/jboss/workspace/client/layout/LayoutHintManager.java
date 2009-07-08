package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.Widget;

public interface LayoutHintManager {
    public LayoutHintProvider getProvider(Widget instance);
    public boolean isManaged(Widget instance);
}
