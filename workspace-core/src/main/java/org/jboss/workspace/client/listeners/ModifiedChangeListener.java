package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.NotificationCallback;

public class ModifiedChangeListener implements ChangeListener {
 //   private StatePacket packet;
    private NotificationCallback notificationCallback;


    public void onChange(Widget widget) {
   //     packet.getTabInstance().setModified(true);

        if (notificationCallback != null) notificationCallback.call();
    }

    public void addMessageCallback(NotificationCallback callback) {
        if (notificationCallback == null) notificationCallback = callback;
        else {
            notificationCallback.addMessageCallback(callback);
        }
    }
}
