package org.jboss.errai.client.widgets.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.client.widgets.WSGrid;

import java.util.LinkedHashSet;
import java.util.Set;

public class WSCellMultiSelector extends WSCellFormatter {
    private Set<String> values;
    //  private HTML selection;
    private boolean updateble = true;

    private static ListBox listBox;
    private static WSCellMultiSelector editCellReference;

    static {
        listBox = new ListBox(false);
        listBox.setVisible(false);
        listBox.getElement().getStyle().setProperty("position", "absolute");

        RootPanel.get().add(listBox);

        listBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int selIndex = listBox.getSelectedIndex();
                editCellReference.setValue(listBox.getItemText(selIndex));
                editCellReference.stopedit();
            }
        });
    }

    public WSCellMultiSelector(Set<String> values) {
        this(values, "");
    }

    public WSCellMultiSelector(Set<String> values, String defaultSelection) {
        this.values = new LinkedHashSet<String>();
        this.html = new HTML();
        if (defaultSelection != null) {
            this.values.add(defaultSelection);
            this.html.setHTML(defaultSelection);
        }
        this.values.addAll(values);

    }

    public void setValue(String value) {
        if (values.contains(value)) {
            super.setValue(value);
        }
        else if (updateble) {
            super.setValue(value);
            values.add(value);
        }
    }

    public boolean edit(WSGrid.WSCell element) {
        wsCellReference = element;
        editCellReference = this;

        Style s = listBox.getElement().getStyle();

        s.setProperty("left", element.getAbsoluteLeft() + "px");
        s.setProperty("top", element.getAbsoluteTop() + "px");

        listBox.clear();

        int i = 0;
        for (String v : values) {
            listBox.addItem(v);
            if (html.getHTML().equals(v)) listBox.setSelectedIndex(i);
            i++;
        }

        listBox.setVisible(true);
        listBox.setFocus(true);
        return true;
    }

    public void stopedit() {
        listBox.setVisible(false);
        wsCellReference.stopedit();
    }
}
