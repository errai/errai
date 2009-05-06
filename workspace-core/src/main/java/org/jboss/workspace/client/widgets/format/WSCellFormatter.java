package org.jboss.workspace.client.widgets.format;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;

public class WSCellFormatter extends Composite {
    private String value;

    public WSCellFormatter(String value) {
        this.value = value;
    }

    public void setTextValue(String value) {
        this.value = value;
    }

    public String getTextValue() {
        return value;
    }

    public Widget getWidget() {
        return new HTML(value);
    }
}
