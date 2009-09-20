package org.jboss.errai.client.widgets;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class WSIconTreeItem {
    public static TreeItem create(Image icon, final String name) {
        TreeItem item;
        
        if (icon != null) {
            item = new TreeItem("<span unselectable=\"on\"><img src=\""
                    + icon.getUrl() + "\" height=\"16\" width=\"16\" align=\"left\"/>"
                    + name + "</span>");
        }
        else {
            item = new TreeItem("<span unselectable=\"on\">" + name + "</span");
        }

        return item;
    }
}