package org.jboss.workspace.client.widgets.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import static com.google.gwt.i18n.client.DateTimeFormat.getShortDateFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.jboss.workspace.client.widgets.WSGrid;

import static java.lang.String.valueOf;
import java.util.Date;

public class WSCellDateFormat extends WSCellFormatter {
    private HTML html;
    private Date date;

    private static DatePicker datePicker;
    private static WSCellDateFormat editCellReference;

    static {
        datePicker = new DatePicker();
        RootPanel.get().add(datePicker);
        datePicker.getElement().getStyle().setProperty("position", "absolute");
        datePicker.setVisible(false);

        datePicker.addValueChangeHandler(new ValueChangeHandler() {
            public void onValueChange(ValueChangeEvent valueChangeEvent) {
                wsCellReference.setValue(valueOf(((Date)valueChangeEvent.getValue()).getTime()));
                datePicker.setVisible(false);
                editCellReference.stopedit();
            }
        });
    }

    public WSCellDateFormat(String value) {                                          
        this.html = new HTML(value);
        setValue(value);
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
        return valueOf(date.getTime());
    }

    public void edit(WSGrid.WSCell element) {
        wsCellReference = element;
        editCellReference = this;

        datePicker.setValue(date);

        Style s = datePicker.getElement().getStyle();
        s.setProperty("left", element.getAbsoluteLeft() + "px");
        s.setProperty("top", element.getAbsoluteTop() + "px");

        datePicker.setVisible(true);
    }

    public void stopedit() {
        datePicker.setVisible(false);
        wsCellReference.stopedit();
    }
}
