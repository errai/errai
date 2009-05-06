package org.jboss.workspace.client.widgets.format;

import static com.google.gwt.i18n.client.DateTimeFormat.getShortDateFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;


public class WSCellDateFormat extends WSCellFormatter {
    public WSCellDateFormat(String value) {
        super(value);
    }

    @Override
    public Widget getWidget() {
        if ("&nbsp;".equals(getTextValue()) && getTextValue() == null) {
            return new HTML("");
        }

        Date dt = new Date(Long.parseLong(getTextValue()));

        return new HTML(getShortDateFormat().format(dt));
    }
}
