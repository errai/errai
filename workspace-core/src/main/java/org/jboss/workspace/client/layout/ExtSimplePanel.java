package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.SimplePanel;

public class ExtSimplePanel extends SimplePanel {

    @Override
    public void setPixelSize(int width, int height) {
        super.setPixelSize(width, height);
        getWidget().setPixelSize(width, height);
    }
}
