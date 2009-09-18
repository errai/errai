package org.jboss.workspace.client.listeners;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.widgets.WSTab;
import org.jboss.workspace.client.rpc.protocols.LayoutCommands;
import org.jboss.workspace.client.rpc.protocols.LayoutParts;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.MessageBusClient;

import java.util.Map;
import java.util.HashMap;


public class TabCloseHandler implements CloseHandler<WSTab>, AcceptsCallback {
    /**
     * The reference to the tab.
     */
    private String instanceId;

    public TabCloseHandler(String instanceId) {
        this.instanceId = instanceId;
    }

    public void onClose(CloseEvent closeEvent) {
        MessageBusClient.store("org.jboss.workspace.WorkspaceLayout",
                CommandMessage.create(LayoutCommands.CloseTab)
                .set(LayoutParts.InstanceID, instanceId));
    }


    /**
     * The callback receiver method for the warning dialog box.
     *
     * @param message
     */
    public void callback(Object message, Object data) {
    }


}
