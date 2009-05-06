package org.jboss.workspace.client.widgets.format;

import static com.google.gwt.i18n.client.DateTimeFormat.getShortDateFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Element;

import java.util.Date;

import org.jboss.workspace.client.widgets.WSGrid;


public class WSCellDateFormat extends WSCellFormatter {
    private HTML html;
    private Date date;

    public WSCellDateFormat(String value) {
        this.html = new HTML(value);
    }

    public WSCellDateFormat(Date date) {
        this.date = date;
        setValue(date);
    }

    @Override
    public Widget getWidget() {
        return html;
    }

    public void setValue(String value) {
        if (value == null || value.length() == 0) {
            html.setHTML("&nbsp;");
            return;
        }

        date = new Date(Long.parseLong(value));

        setValue(date);
    }

    public void setValue(Date date) {
        html.setHTML(getShortDateFormat().format(date));
    }

    public String getTextValue() {
        return String.valueOf(date.getTime());
    }

    public void edit(WSGrid.WSCell element) {

    }

    public void stopedit() {
    }
}
