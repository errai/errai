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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.widgets.client.WSGrid;

public class WSCellSimpleTextCell extends WSCellFormatter<String> {
    private static TextBox textBox;
    private static WSCellSimpleTextCell editCellReference;

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
        this.html.setStyleName("WSCell-inner");
    }

    public WSCellSimpleTextCell(String value, boolean readonly) {
        this.html = new HTML(value);
        this.html.setStyleName("WSCell-inner");
        
        this.readonly = readonly;
    }

    public boolean edit(WSGrid.WSCell element) {
        if (readonly) return false;
        
        editCellReference = this;
        wsCellReference = element;

        textBox.setText(getTextValue());
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

    @Override
    public String getValue() {
        return html.getText();
    }
}
