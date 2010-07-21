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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.icons.ErraiWidgetsImageBundle;


public class WSCellTitle extends WSCellFormatter<String> {
    HorizontalPanel hPanel = new HorizontalPanel();
    HTML label = new HTML();
    Image img = new Image();

    WSGrid.WSCell cell;

    ErraiWidgetsImageBundle imageBundle = GWT.create(ErraiWidgetsImageBundle.class);

    public WSCellTitle(WSGrid.WSCell cell, String title) {
        this.cell = cell;

        label.setHTML(title);
        hPanel.add(label);
        hPanel.add(img);
        hPanel.setWidth("100%");
        hPanel.setCellWidth(img, "16px");

        img.setVisible(false);
        img.getElement().getStyle().setProperty("textAlign", "right");

    }

    public void setValue(String value) {
        label.setHTML(value);
    }

    @Override
    public String getValue() {
        return label.getText();
    }

    public String getTextValue() {
        return label.getHTML();
    }

    public Widget getWidget(WSGrid wsGrid) {
        if (wsGrid.getSortedColumnHeader() == cell) {

            if (wsGrid.getColumnSortOrder(cell.getCol()))
                img.setResource(imageBundle.sortDown());
            else
                img.setResource(imageBundle.sortUp());

            img.setVisible(true);
        }
        else {
            img.setVisible(false);
        }

        return hPanel;
    }

    @Override
    public void setHeight(String height) {
    }

    @Override
    public void setWidth(String width) {
    }

    public boolean edit(WSGrid.WSCell element) {
        return false;
    }

    public void stopedit() {
    }
}
