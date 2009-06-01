package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.*;


public class WSTreeItem extends TreeItem {
    public WSTreeItem(Image icon, String html) {
        super();

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(icon);
        hPanel.add(new HTML(html));

        setWidget(hPanel);
    }

    public WSTreeItem(Image icon, Widget widget) {
        super();

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(icon);
        hPanel.add(widget);

        setWidget(hPanel);
    }
}
