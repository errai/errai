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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.common.client.ErraiCommon;
import org.jboss.errai.workspaces.client.layout.WorkspaceLayout;

public class WSLauncherPanel extends VerticalPanel {

    private boolean armed;
    private WorkspaceLayout workspaceLayout;

    Timer t = new Timer() {
        public void run() {
            if (armed) workspaceLayout.openNavPanel();
        }
    };

    int range = -1;
    
    public WSLauncherPanel(WorkspaceLayout layout) {
        super();

        sinkEvents(Event.MOUSEEVENTS);

        this.workspaceLayout = layout;

        ErraiCommon.disableTextSelection(getElement(), true);
    }


    public void onBrowserEvent(Event event) {
        if (!armed) return;

        switch (event.getTypeInt()) {
            case Event.ONMOUSEOVER:
                break;
            case Event.ONMOUSEUP:
                if (range == -1) {
                    range = getAbsoluteTop() + 20;
                }

                if (event.getClientY() > range) {
                    t.cancel();
                    workspaceLayout.openNavPanel();
                }
                break;

            case Event.ONCLICK:
                break;

            case Event.ONMOUSEMOVE:

                if (range == -1) {
                    range = getAbsoluteTop() + 20;
                }

                if (event.getClientY() > range) {
                    setStyleName("workspace-LeftNavArea-MouseOver");
                    t.schedule(200);
                }

                break;


            case Event.ONMOUSEOUT:
                Element to = DOM.eventGetToElement(event);
                if (to == null || !DOM.isOrHasChild(this.getElement(), to)) {
                    setStyleName("workspace-LeftNavArea");
                    t.cancel();
                    workspaceLayout.collapseNavPanel();
                }
                break;
        }
    }


    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }
}
