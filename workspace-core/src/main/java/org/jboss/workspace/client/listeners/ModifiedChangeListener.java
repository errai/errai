package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.framework.MessageCallback;
import org.gwt.mosaic.ui.client.infopanel.TrayInfoPanelNotifier;

public class ModifiedChangeListener implements ChangeListener {
    private StatePacket packet;
    private MessageCallback messageCallback;

    public ModifiedChangeListener(StatePacket packet) {
        this.packet = packet;
    }

    public void onChange(Widget widget) {
        packet.getActiveLayout().findTab(packet.getInstanceId()).setModified(true);
        if (messageCallback != null) messageCallback.call();
        TrayInfoPanelNotifier.notifyTrayEvent("Draft Saved", "Draft data has been saved on the server.");
    }

    public void addMessageCallback(MessageCallback callback) {
        if (messageCallback == null) messageCallback = callback;
        else {
            messageCallback.addMessageCallback(callback);
        }
    }
}
