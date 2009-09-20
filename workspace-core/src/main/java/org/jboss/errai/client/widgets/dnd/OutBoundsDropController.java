package org.jboss.errai.client.widgets.dnd;

import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by IntelliJ IDEA.
 * User: christopherbrock
 * Date: Aug 29, 2008
 * Time: 8:46:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class OutBoundsDropController extends AbstractDropController {
    public OutBoundsDropController(Widget widget) {
        super(widget);
    }

    public void onPreviewDrop(DragContext dragContext) throws VetoDragException {
        throw new VetoDragException();
    }
}
