package org.jboss.workspace.client.widgets.format;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;
import org.jboss.workspace.client.widgets.WSGrid;

import java.util.Date;

//todo: this totally needs to be refactored... the formatter currently holds the value...
public abstract class WSCellFormatter {
    protected static WSGrid.WSCell wsCellReference;
    protected HTML html;
    protected boolean readonly = false;

    public void setValue(String value) {
        if (readonly) return;

        notifyCellUpdate(value);

        if (value == null || value.length() == 0) {
            html.setHTML("&nbsp;");
            return;
        }

        html.setHTML(value);
    }

    public String getTextValue() {
        return html.getHTML().equals("&nbsp;") ? "" : html.getHTML();
    }

    public Widget getWidget(WSGrid grid) {
        return html;
    }
    
    public abstract boolean edit(WSGrid.WSCell element);
    public abstract void stopedit();

    public void setHeight(String height) {
        html.setHeight(height);
    }

    public void setWidth(String width) {
        html.setWidth(width);
    }


    /**
     * Notify any registered listeners that the value is about to change.
     * @param newValue
     */
    public void notifyCellUpdate(String newValue) {
        if (wsCellReference == null) return;

        wsCellReference.notifyCellUpdate(newValue);
    }
}
