package org.jboss.errai.workspaces.client.listeners;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.protocols.LayoutCommands;
import org.jboss.errai.bus.client.protocols.LayoutParts;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.workspaces.client.widgets.WSTab;


public class TabCloseHandler implements CloseHandler<WSTab>, AcceptsCallback {
    /**
     * The reference to the tab.
     */
    private String instanceId;

    public TabCloseHandler(String instanceId) {
        this.instanceId = instanceId;
    }

    public void onClose(CloseEvent closeEvent) {
        CommandMessage.create(LayoutCommands.CloseTab)
                .toSubject("org.jboss.errai.WorkspaceLayout")
                .set(LayoutParts.InstanceID, instanceId)
                .sendNowWith(ErraiBus.get());
    }


    /**
     * The callback receiver method for the warning dialog box.
     *
     * @param message
     */
    public void callback(Object message, Object data) {
    }
}
