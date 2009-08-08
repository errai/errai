package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.MessageCallback;

public class ModifiedChangeListener implements ChangeListener {
 //   private StatePacket packet;
    private MessageCallback messageCallback;


    public void onChange(Widget widget) {
   //     packet.getTabInstance().setModified(true);

        if (messageCallback != null) messageCallback.call();
    }

    public void addMessageCallback(MessageCallback callback) {
        if (messageCallback == null) messageCallback = callback;
        else {
            messageCallback.addMessageCallback(callback);
        }
    }
}
