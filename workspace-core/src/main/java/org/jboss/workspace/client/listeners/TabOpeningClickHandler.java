package org.jboss.workspace.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.workspace.client.framework.CommandProcessor;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.StatePacket;

import java.util.HashMap;
import java.util.Map;

public class TabOpeningClickHandler implements ClickHandler {
    private String tabName;
    private Tool tool;
    private Image icon;
    private boolean multipleAllowed;

    public TabOpeningClickHandler(Tool tool) {
        this.tabName = tool.getName();
        this.tool = tool;
        this.icon = tool.getIcon();
    }

    public TabOpeningClickHandler(String tabName, Tool tool, Image icon) {
        this.tabName = tabName;
        this.tool = tool;
        this.icon = icon;
    }

    public TabOpeningClickHandler(String tabName, Tool tool, Image icon,
                                  boolean multipleAllowed) {
        this.tabName = tabName;
        this.tool = tool;
        this.icon = icon;
        this.multipleAllowed = multipleAllowed;
    }

    public void onClick(ClickEvent event) {

        /**
         * Build the message to send the command processor.
         */
        Map<String,Object> msg = new HashMap<String, Object>();
        msg.put(CommandProcessor.MessageParts.ComponentID.name(),        tool.getId());
        msg.put(CommandProcessor.MessageParts.IconURI.name(),            tool.getIcon().getUrl());
        msg.put(CommandProcessor.MessageParts.MultipleInstances.name(),  tool.multipleAllowed());
        msg.put(CommandProcessor.MessageParts.Name.name(),               tool.getName());

        Widget w = tool.getWidget();

        String DOMID = tool.getId() + "_" + System.currentTimeMillis();
        w.getElement().setId(DOMID);
        w.setVisible(false);
        RootPanel.get().add(w);

        msg.put(CommandProcessor.MessageParts.DOMID.name(),             DOMID);
           
        CommandProcessor.Command.OpenNewTab.send(msg);
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
