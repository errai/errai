package org.jboss.errai.client.listeners;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import org.jboss.errai.client.framework.AcceptsCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.MessageBusClient;
import org.jboss.errai.client.rpc.protocols.LayoutCommands;
import org.jboss.errai.client.rpc.protocols.LayoutParts;
import org.jboss.errai.client.widgets.WSTab;


public class TabCloseHandler implements CloseHandler<WSTab>, AcceptsCallback {
    /**
     * The reference to the tab.
     */
    private String instanceId;

    public TabCloseHandler(String instanceId) {
        this.instanceId = instanceId;
    }

    public void onClose(CloseEvent closeEvent) {
        MessageBusClient.send("org.jboss.errai.WorkspaceLayout",
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
