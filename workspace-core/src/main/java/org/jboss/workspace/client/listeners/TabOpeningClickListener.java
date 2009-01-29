package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import static org.jboss.workspace.client.Workspace.WORKSPACE;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.StatePacket;

public class TabOpeningClickListener implements ClickListener {
    private String tabName;
    private Tool tool;
    private Image icon;
    private boolean multipleAllowed;

    public TabOpeningClickListener(String tabName, Tool tool, Image icon) {
        this.tabName = tabName;
        this.tool = tool;
        this.icon = icon;
    }

    public TabOpeningClickListener(String tabName, Tool tool, Image icon, boolean multipleAllowed) {
        this.tabName = tabName;
        this.tool = tool;
        this.icon = icon;
        this.multipleAllowed = multipleAllowed;
    }

    public void onClick(Widget sender) {
        //todo: abstract this better.
        WORKSPACE.openTab(tool, new StatePacket(tool), icon, multipleAllowed);
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public boolean isMultipleAllowed() {
        return multipleAllowed;
    }

    public void setMultipleAllowed(boolean multipleAllowed) {
        this.multipleAllowed = multipleAllowed;
    }
}
