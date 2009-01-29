package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.listeners.TabOpeningClickListener;


/**
 * A simiple dock area to list and provide links to different features.
 */
public class WSLauncherPanel extends Composite {
    private VerticalPanel vPanel;

    public WSLauncherPanel() {
        this.vPanel = new VerticalPanel();
        this.vPanel.setWidth("100%");
        initWidget(vPanel);
    }

    public void addLink(String name, Tool tool) {
        Image newIcon = new Image(tool.getIcon().getUrl());
        newIcon.setSize("16px", "16px");

        WSLaunchButton button = new WSLaunchButton(newIcon, name);
        button.addClickListener(new TabOpeningClickListener(name, tool, newIcon, tool.multipleAllowed()));
        vPanel.add(button);

    }
}
