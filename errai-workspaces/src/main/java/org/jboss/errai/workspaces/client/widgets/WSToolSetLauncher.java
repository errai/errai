/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.widgets.client.WSLaunchButton;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.listeners.TabOpeningClickHandler;


/**
 * A simiple dock area to list and provide links to different features.
 */
public class WSToolSetLauncher extends Composite {
    private VerticalPanel vPanel;

    ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

    public WSToolSetLauncher() {
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
            newIcon = new Image(erraiImageBundle.questionCube());
        }
        
        newIcon.setSize("16px", "16px");

        WSLaunchButton button = new WSLaunchButton(newIcon, name);
        button.addClickListener(new TabOpeningClickHandler(tool));
        vPanel.add(button);
    }
}
