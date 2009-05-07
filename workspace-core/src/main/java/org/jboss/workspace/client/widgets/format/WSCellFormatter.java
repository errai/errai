package org.jboss.workspace.client.widgets.format;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.widgets.WSGrid;

//todo: this totally needs to be refactored... the formatter currently holds the value...
public abstract class WSCellFormatter {
    protected static WSGrid.WSCell wsCellReference;

    public abstract void setValue(String value);
    public abstract String getTextValue();
    public abstract Widget getWidget();
    public abstract void edit(WSGrid.WSCell element);
    public abstract void stopedit();

    public abstract WSCellFormatter newFormatter();
}
