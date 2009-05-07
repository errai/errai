package org.jboss.workspace.client.widgets.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.widgets.WSGrid;

import java.util.Set;
import java.util.LinkedHashSet;

public class WSCellMultiSelector extends WSCellFormatter {
    private Set<String> values;
    private HTML selection;
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
        this.selection = new HTML();
        if (defaultSelection != null) {
            this.values.add(defaultSelection);
            this.selection.setHTML(defaultSelection);
        }
        this.values.addAll(values);

    }

    public void setValue(String value) {
        if (values.contains(value)) {
            this.selection.setHTML(value);
        }
        else if (updateble) {
            values.add(value);
            this.selection.setHTML(value);
        }
    }

    public String getTextValue() {
        return selection.getHTML();
    }

    public Widget getWidget() {
        return selection;
    }

    public void edit(WSGrid.WSCell element) {
        wsCellReference = element;
        editCellReference = this;

        Style s = listBox.getElement().getStyle();

        s.setProperty("left", element.getAbsoluteLeft() + "px");
        s.setProperty("top", element.getAbsoluteTop() + "px");

        listBox.clear();

        int i = 0;
        for (String v : values) {
            listBox.addItem(v);
            if (selection.getHTML().equals(v)) listBox.setSelectedIndex(i);
            i++;
        }
        
        listBox.setVisible(true);
        listBox.setFocus(true);
    }

    public void stopedit() {
        listBox.setVisible(false);
        wsCellReference.stopedit();
    }
}
