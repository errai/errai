package org.jboss.errai.workspaces.client.layout;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class PanelHintReference implements LayoutHintProvider {
    private Panel p;

    public PanelHintReference(Panel p) {
        this.p = p;
    }

    public int getHeightHint() {
        return p.getOffsetHeight();
    }

    public int getWidthHint() {
        return p.getOffsetWidth();
    }
}
