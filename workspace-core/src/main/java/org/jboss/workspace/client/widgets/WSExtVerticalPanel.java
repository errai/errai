package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import org.gwt.mosaic.core.client.DOM;

public class WSExtVerticalPanel extends VerticalPanel {
    private MouseListener mouseListener;
    private boolean armed;

    public WSExtVerticalPanel() {
        super();

        sinkEvents(Event.MOUSEEVENTS);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (!armed) return;
        if (mouseListener == null) return;

        switch (event.getTypeInt()) {
            case Event.ONMOUSEOVER:
                mouseListener.onMouseEnter(null);
                break;
            case Event.ONMOUSEUP:
                mouseListener.onMouseUp(null, event.getClientX(), event.getClientY());
                break;

            case Event.ONCLICK:
                break;

            case Event.ONMOUSEMOVE:
                mouseListener.onMouseMove(null, event.getClientX(), event.getClientY());
                break;


            case Event.ONMOUSEOUT:                
                Element to = DOM.eventGetToElement(event);
                if (to == null || !DOM.isOrHasChild(this.getElement(), to)) mouseListener.onMouseLeave(null);
                break;
        }
    }

    public MouseListener getMouseListener() {
        return mouseListener;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }
}
