package org.jboss.errai.client.widgets.dnd;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import org.jboss.errai.client.layout.WorkspaceLayout;
import org.jboss.errai.client.widgets.WSTab;
import org.jboss.errai.client.widgets.WSTabPanel;

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
