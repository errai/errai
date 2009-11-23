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

package org.jboss.errai.workspaces.client.widgets.dnd;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import org.jboss.errai.workspaces.client.widgets.WSTab;
import org.jboss.errai.workspaces.client.widgets.WSTabPanel;

public class TabDropController extends AbstractDropController {
    WSTabPanel tp;
    WSTab targetTab;
    int startX;

    public TabDropController(WSTabPanel widget, WSTab targetTab) {
        super(targetTab);
        tp = widget;
        this.targetTab = targetTab;
    }

    public void onDrop(DragContext dragContext) {
        WSTab sourceTab = (WSTab) dragContext.draggable;
        /**
         * The original position of the tab
         */
        int sourceIdx = tp.getWidgetIndex(sourceTab.getWidgetRef());

        /**
         * The requested position of the tab
         */
        int destIdx = tp.getWidgetIndex(targetTab.getWidgetRef());

        if (sourceIdx < destIdx) {
            // moving right
            if (destIdx == (tp.getWidgetCount() - 1)) {
                // the target is the right-most targetTab
                tp.remove(sourceTab.getWidgetRef());
                tp.remove(targetTab.getWidgetRef());

                tp.add(targetTab.getWidgetRef(), targetTab);
                tp.add(sourceTab.getWidgetRef(), sourceTab);
            }
            else {
                tp.insert(sourceTab.getWidgetRef(), sourceTab, destIdx + 1);
            }
        }
        else {
            /**
             * We're moving left
             */
            tp.remove(sourceIdx);

            if (tp.getWidgetCount() <= destIdx) {
                tp.add(sourceTab.getWidgetRef(), sourceTab);
                tp.add(targetTab.getWidgetRef(), targetTab);
            }
            else {
                tp.insert(sourceTab.getWidgetRef(), sourceTab, destIdx);
                tp.insert(targetTab.getWidgetRef(), targetTab, destIdx + 1);
            }
        }

        tp.selectTab(destIdx);
        
        sourceTab.reset();
        targetTab.reset();

        super.onDrop(dragContext);
    }

    public void onEnter(DragContext dragContext) {
        super.onEnter(dragContext);
    }

    public void onLeave(DragContext dragContext) {
        super.onLeave(dragContext);
    }

    public void onMove(DragContext dragContext) {
        super.onMove(dragContext);
    }

    public void onPreviewDrop(DragContext dragContext) throws VetoDragException {
        super.onPreviewDrop(dragContext);
    }
}
