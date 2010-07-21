/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.widgets.client.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.jboss.errai.widgets.client.WSGrid;

import java.util.Date;

import static java.lang.String.valueOf;


public class WSCellDateFormat extends WSCellFormatter<Date> {
    private Date date;
    private String formatPattern = "MMM dd, yyyy";

    private static DatePicker datePicker;
    private static WSCellDateFormat editCellReference;

    static {
        datePicker = new DatePicker();
        RootPanel.get().add(datePicker);
        datePicker.getElement().getStyle().setProperty("position", "absolute");
        datePicker.setVisible(false);

        datePicker.addValueChangeHandler(new ValueChangeHandler() {
            public void onValueChange(ValueChangeEvent valueChangeEvent) {
                ((WSCellDateFormat)wsCellReference.getCellFormat()).setValue((Date) valueChangeEvent.getValue());
              //  wsCellReference.setValue(valueOf(((Date) valueChangeEvent.getValue()).getTime()));
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
        this.html = new HTML();
        setValue(date);
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public void setValue(String value) {
        if (value == null || value.length() == 0) {
            return;
        }

        setValue(new Date(Long.parseLong(value)));
    }

    public void setValue(Date date) {
        notifyCellUpdate(date);
        this.date = date;
        if (date != null) html.setHTML(DateTimeFormat.getFormat(formatPattern).format(date));
        notifyCellAfterUpdate();
    }

    @Override
    public Date getValue() {
        return date;
    }

    @Override
    public String getTextValue() {
        return date == null ? null : valueOf(date.getTime());
    }


    public boolean edit(WSGrid.WSCell element) {
        wsCellReference = element;
        editCellReference = this;

        datePicker.setValue(date);
        datePicker.setCurrentMonth(date);

        Style s = datePicker.getElement().getStyle();

        int left = (element.getAbsoluteLeft() + element.getOffsetWidth() - 20);

        if ((left + datePicker.getOffsetWidth()) > Window.getClientHeight()) {
             left = Window.getClientHeight() - datePicker.getOffsetHeight();
        }

        s.setProperty("left", left + "px");
        s.setProperty("top", (element.getAbsoluteTop() + element.getOffsetHeight()) + "px");

        datePicker.setVisible(true);
        return true;
    }

    public void stopedit() {
        datePicker.setVisible(false);
        wsCellReference.stopedit();
    }
}
