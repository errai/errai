package org.jboss.workspace.client.widgets.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.widgets.WSGrid;


public class WSCellSimpleTextCell extends WSCellFormatter {
    private HTML html;

    private static TextBox textBox;
    private static WSCellSimpleTextCell editCellReference;
    private boolean readonly = false;

    static {
        textBox = new TextBox();
        textBox.setStylePrimaryName("WSCell-editbox");
        textBox.setVisible(false);

        RootPanel.get().add(textBox);

        textBox.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                switch (event.getCharCode()) {
                    case KeyCodes.KEY_ESCAPE:
                        editCellReference.stopedit();
                        wsCellReference.focus();
                        break;
                    case KeyCodes.KEY_TAB:
                        editCellReference.stopedit();
                        break;
                    case KeyCodes.KEY_ENTER:
                        editCellReference.stopedit();
                        break;
                }
            }
        });                                                                                                                                               

    }

    public WSCellSimpleTextCell(String value) {
        this.html = new HTML(value);
    }

    public WSCellSimpleTextCell(String value, boolean readonly) {
        this.html = new HTML(value);
        this.readonly = readonly;
    }

    public void setValue(String value) {
        if (readonly) return;
        this.html.setHTML(value);
    }

    public String getTextValue() {
        return this.html.getHTML();
    }

    public Widget getWidget(WSGrid wsGrid) {
        return html;
    }

    public boolean edit(WSGrid.WSCell element) {
        if (readonly) return false;
        
        editCellReference = this;
        wsCellReference = element;

        textBox.setText(html.getHTML());
        textBox.setVisible(true);

        Style s = textBox.getElement().getStyle();

        s.setProperty("left", element.getAbsoluteLeft() + "px");
        s.setProperty("top", element.getAbsoluteTop() + "px");

        textBox.setSize(element.getOffsetWidth() + "px", element.getOffsetHeight() + "px");

        textBox.setCursorPos(textBox.getText().length());
        textBox.setFocus(true);

        return true;
    }

    public void stopedit() {
        if (!readonly) setValue(textBox.getText());

        textBox.setVisible(false);
        wsCellReference.stopedit();
    }

    public WSCellFormatter newFormatter() {
        return new WSCellSimpleTextCell("");
    }
}
