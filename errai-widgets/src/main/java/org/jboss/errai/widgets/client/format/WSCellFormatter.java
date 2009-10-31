package org.jboss.errai.widgets.client.format;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.WSGrid;

//todo: this totally needs to be refactored... the formatter currently holds the value...
public abstract class WSCellFormatter<T> {
    protected static WSGrid.WSCell wsCellReference;
    protected HTML html;
    protected boolean readonly = false;
    protected boolean cancel = false;

    public void cancelEdit() {
        cancel = true;
    }

    public void setValue(T value) {
        try {
            if (readonly) return;

            String str = String.valueOf(value);

            notifyCellUpdate(str);

            if (!cancel) {
                if (value == null || str.length() == 0) {
                    html.setHTML("&nbsp;");
                    return;
                }

                html.setHTML(str);
            } else
                cancel = false;

        }
        finally {
            notifyCellAfterUpdate();
        }
    }

    public String getTextValue() {
        return html.getHTML().equals("&nbsp;") ? "" : html.getHTML();
    }

    public abstract T getValue();

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
     *
     * @param newValue
     */
    public void notifyCellUpdate(Object newValue) {
        if (wsCellReference == null) return;
        wsCellReference.notifyCellUpdate(newValue);
    }

    public void notifyCellAfterUpdate() {
        if (wsCellReference == null) return;
        wsCellReference.notifyCellAfterUpdate();
    }
}
