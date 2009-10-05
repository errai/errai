package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.listeners.TabOpeningClickHandler;


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
        Image newIcon;
        if (tool.getIcon() != null) {
            newIcon = new Image(tool.getIcon().getUrl());
        }
        else {
            newIcon = new Image(GWT.getModuleBaseURL() + "/images/ui/icons/questioncube.png");
        }
        
        newIcon.setSize("16px", "16px");

        WSLaunchButton button = new WSLaunchButton(newIcon, name);
        button.addClickListener(new TabOpeningClickHandler(tool));
        vPanel.add(button);
    }
}
