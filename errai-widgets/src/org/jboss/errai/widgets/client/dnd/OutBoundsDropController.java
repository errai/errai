package org.jboss.errai.widgets.client.dnd;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.google.gwt.user.client.ui.Widget;

public class OutBoundsDropController extends AbstractDropController {
    public OutBoundsDropController(Widget widget) {
        super(widget);
    }

    public void onPreviewDrop(DragContext dragContext) throws VetoDragException {
        throw new VetoDragException();
    }
}
