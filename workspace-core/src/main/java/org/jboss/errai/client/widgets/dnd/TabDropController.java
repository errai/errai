package org.jboss.errai.client.widgets.dnd;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import org.jboss.errai.client.widgets.WSTab;
import org.jboss.errai.client.widgets.WSTabPanel;

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
