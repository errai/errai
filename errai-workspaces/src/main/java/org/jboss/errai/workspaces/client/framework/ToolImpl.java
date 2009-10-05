package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ToolImpl implements Tool {
    private String name;
    private String id;
    private boolean multipleAllowed;
    private Image icon;
    private WSComponent component;

    public ToolImpl(String name, String id, boolean multipleAllowed, Image icon, WSComponent component) {
        this.name = name;
        this.id = id;
        this.multipleAllowed = multipleAllowed;
        this.icon = icon;
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean multipleAllowed() {
        return multipleAllowed;
    }

    public Image getIcon() {
        return icon;
    }

    public Widget getWidget() {
        return component.getWidget();
    }
}
