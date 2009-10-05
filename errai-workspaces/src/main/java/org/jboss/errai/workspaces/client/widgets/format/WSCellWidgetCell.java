package org.jboss.errai.workspaces.client.widgets.format;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.workspaces.client.widgets.WSGrid;


public class WSCellWidgetCell extends WSCellFormatter {

    Widget widget = null;

    public WSCellWidgetCell(Widget widget) {
        this.widget = widget;
        this.readonly = false;
    }


    public boolean edit(WSGrid.WSCell element) {
        return false;
    }

    public void stopedit() {
        // do nothing
    }

    public String getTextValue() {
        return widget.toString();
    }

    public Widget getWidget(WSGrid grid) {
        return widget;
    }

    public void setHeight(String height) {
        widget.setHeight(height);
    }

    public void setWidth(String width) {
        widget.setWidth(width);
    }

}
