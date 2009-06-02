package org.jboss.workspace.client.widgets;

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
