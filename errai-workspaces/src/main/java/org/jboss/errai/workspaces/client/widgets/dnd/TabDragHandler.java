/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.widgets.dnd;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import org.jboss.errai.workspaces.client.layout.WorkspaceLayout;
import org.jboss.errai.workspaces.client.widgets.WSTab;
import org.jboss.errai.workspaces.client.widgets.WSTabPanel;

public class TabDragHandler implements DragHandler {
    private WorkspaceLayout layout;

    public TabDragHandler(WorkspaceLayout layout) {
        this.layout = layout;
    }

    /**
     * Log the drag end event.
     *
     * @param event the event to log
     */                           
    public void onDragEnd(DragEndEvent event) {
        WSTab tab = (WSTab) event.getContext().draggable;

        WSTabPanel panel = layout.tabPanel;
        /**
         * If the tab has been dragged outside the boundaries, we need to catch it, so it doesn't get thrown
         * away.
         */
        if (panel.getWidgetIndex(tab.getWidgetRef()) == -1) {
            panel.add(panel.getWidget(panel.getWidgetCount()-1),tab);
        }
    }

    /**
     * Log the drag start event.
     *
     * @param event the event to log
     */
    public void onDragStart(DragStartEvent event) {
    }

    /**
     * Log the preview drag end event.
     *
     * @param event the event to log
     * @throws VetoDragException exception which may be thrown by any drag handler
     */
    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
    }

    /**
     * Log the preview drag start event.
     *
     * @param event the event to log
     * @throws VetoDragException exception which may be thrown by any drag handler
     */
    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
    }

}
