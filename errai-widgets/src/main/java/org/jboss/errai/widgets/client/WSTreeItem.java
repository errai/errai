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

package org.jboss.errai.widgets.client;

import com.google.gwt.user.client.ui.*;

public class WSTreeItem extends TreeItem {
    private Image icon;
    private String label;

    public WSTreeItem(Widget widget) {
        super();
        setWidget(widget);
    }

    public WSTreeItem(Image icon, String html) {
        super();
        this.icon = icon;
        this.label = html;

        setHTML(createHTML());
    }

    @Override
    public WSTreeItem addItem(String itemText) {
        WSTreeItem treeItem = new WSTreeItem(new HTML(itemText));
        super.addItem(treeItem);
        return treeItem;
    }

    @Override
    public WSTreeItem addItem(Widget widget) {
        WSTreeItem treeItem = new WSTreeItem(widget);
        super.addItem(treeItem);
        return treeItem;
    }

    public WSTreeItem addItem(Image icon, Widget content) {
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(icon);
        hPanel.add(content);

        hPanel.setCellWidth(icon, "16px");

        WSTreeItem item = new WSTreeItem(hPanel);
        super.addItem(item);
        return item;
    }

    private String createHTML() {
        return "<span><img src='" + icon.getUrl() + "' align='left'/> " + label + "</span>";
    }
}
