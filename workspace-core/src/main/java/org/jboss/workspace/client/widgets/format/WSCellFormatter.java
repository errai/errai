package org.jboss.workspace.client.widgets.format;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.widgets.WSGrid;

public abstract class WSCellFormatter {
    public abstract void setValue(String value);
    public abstract String getTextValue();
    public abstract Widget getWidget();
    public abstract void edit(WSGrid.WSCell element);
    public abstract void stopedit();
}
